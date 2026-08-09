import { useState, useEffect } from 'react';
import { Swords } from 'lucide-react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

export default function DuelLobby({ currentUser, onMatchStart }) {
  const [isSearching, setIsSearching] = useState(false);
  const [matchFound, setMatchFound] = useState(null);
  const [stompClient, setStompClient] = useState(null);
  const [matchResult, setMatchResult] = useState(null); 

  useEffect(() => {
    if (!currentUser) return;

    setIsSearching(false);
    setMatchFound(null);
    setMatchResult(null);

    const API_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";
    const socket = new SockJS(`${API_URL}/ws`);
    const client = new Client({
      webSocketFactory: () => socket,
      onConnect: () => {
        console.log('Connected to Arena WebSocket Server');
        setStompClient(client);

        client.subscribe(`/topic/match/${currentUser.id}`, (message) => {
          const matchData = JSON.parse(message.body);
          setIsSearching(false);
          setMatchFound(matchData);

          client.subscribe(`/topic/duel/${matchData.roomId}`, (endMessage) => {
            const endData = JSON.parse(endMessage.body);

            if (endData.type === 'MATCH_OVER') {
              if (endData.winnerId === currentUser.id) {
                setMatchResult('WON');
              } else {
                setMatchResult('LOST');
              }

              setTimeout(() => {
                setMatchResult(null);
                setMatchFound(null);
              }, 4000);
            }
          });
        });
      },
      onStompError: (frame) => console.error('Broker error: ' + frame.headers['message'])
    });

    client.activate();
    return () => client.deactivate(); 
  }, [currentUser, setIsSearching, setMatchFound, setMatchResult]);

  const joinQueue = () => {
    if (stompClient && stompClient.connected) {
      setIsSearching(true);
      stompClient.publish({
        destination: '/app/joinQueue',
        body: JSON.stringify({ userId: currentUser.id, username: currentUser.username })
      });
    }
  };

  if (!currentUser) return <div className="text-white p-10 text-center font-semibold tracking-wider text-xl">PLEASE SIGN IN TO DUEL.</div>;

  if (matchResult === 'WON') {
    return (
      <div className="flex flex-col items-center justify-center h-[75vh] text-white z-10 relative">
        <div className="bg-green-900/80 p-16 rounded-3xl border-4 border-green-400 text-center shadow-[0_0_80px_rgba(74,222,128,0.4)] animate-in fade-in zoom-in duration-500">
          <h1 className="text-7xl font-black text-green-400 mb-4 tracking-widest drop-shadow-lg">🏆 YOU WON!</h1>
          <h2 className="text-4xl font-bold text-white mb-6">+5 Points</h2>
          <p className="text-xl text-green-200 animate-pulse">Returning to lobby...</p>
        </div>
      </div>
    );
  }

  if (matchResult === 'LOST') {
    return (
      <div className="flex flex-col items-center justify-center h-[75vh] text-white z-10 relative">
        <div className="bg-red-950/80 p-16 rounded-3xl border-4 border-red-500 text-center shadow-[0_0_80px_rgba(248,113,113,0.4)] animate-in fade-in zoom-in duration-500">
          <h1 className="text-7xl font-black text-red-500 mb-4 tracking-widest drop-shadow-lg">💀 YOU LOST.</h1>
          <h2 className="text-4xl font-bold text-white mb-6">-3 Points</h2>
          <p className="text-xl text-red-200 animate-pulse">Returning to lobby...</p>
        </div>
      </div>
    );
  }

  if (matchFound) {
    return (
      <div className="flex flex-col items-center justify-center h-[75vh] text-white z-10 relative">
        <div className="bg-purple-950/40 p-12 rounded-2xl border-2 border-purple-500 text-center animate-pulse shadow-[0_0_50px_rgba(168,85,247,0.2)]">
          <h1 className="text-6xl font-black text-purple-400 mb-6 tracking-widest drop-shadow-md">MATCH FOUND</h1>
          <div className="text-3xl mb-8 flex items-center justify-center gap-6 font-bold">
            <span className="text-blue-400">{currentUser.username}</span>
            <Swords size={40} className="text-gray-400" />
            <span className="text-red-400">{matchFound.opponent}</span>
          </div>
          <button 
            onClick={() => {
              // --- DIAGNOSTIC LOGS ---
              console.log("🔥 CLICKED 'ENTER ARENA'!");
              console.log("📦 RAW MATCH DATA FROM WEBSOCKET:", matchFound);
              
              if (matchFound && matchFound.opponentId) {
                localStorage.setItem("activeOpponentId", String(matchFound.opponentId));
                console.log("✅ SUCCESSFULLY SAVED TO LOCAL STORAGE:", matchFound.opponentId);
              } else {
                console.error("❌ ERROR: OPPONENT ID IS MISSING FROM MATCH DATA!");
              }
              // -----------------------

              onMatchStart(matchFound.problemId, matchFound.roomId, matchFound.opponentId);
            }}
            className="bg-purple-600 hover:bg-purple-500 text-white px-10 py-4 rounded-xl font-black text-xl uppercase tracking-widest transition-transform hover:scale-105 shadow-lg cursor-pointer"
          >
            Enter Arena (Task #{matchFound.problemId})
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="flex flex-col items-center justify-center h-[75vh] text-white z-10 relative">
      <Swords size={100} className={`mb-8 drop-shadow-xl ${isSearching ? 'text-purple-500 animate-bounce' : 'text-gray-600'}`} />
      <h2 className="text-4xl font-extrabold mb-10 tracking-tight uppercase">Ranked 1v1 Matchmaking</h2>

      <button 
        onClick={joinQueue}
        disabled={isSearching}
        className={`px-12 py-5 rounded-full font-bold text-xl uppercase tracking-widest transition-all ${
          isSearching 
            ? 'bg-gray-900 border-2 border-purple-500/50 text-purple-400 cursor-wait shadow-[0_0_30px_rgba(168,85,247,0.3)]' 
            : 'bg-purple-600 hover:bg-purple-500 text-white shadow-[0_0_30px_rgba(168,85,247,0.4)] hover:-translate-y-1 cursor-pointer'
        }`}
      >
        {isSearching ? "SEARCHING FOR OPPONENT..." : "FIND MATCH"}
      </button>
    </div>
  );
}