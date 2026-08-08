import  { useState, useEffect } from 'react';
import { Swords, Trophy, Target, Activity } from 'lucide-react';

export default function Profile({ currentUser }) {
  const [liveUser, setLiveUser] = useState(currentUser);
  const [isLoading, setIsLoading] = useState(true);

  
  const API_URL = import.meta.env.VITE_API_BASE_URL ||"http://localhost:8080";
  useEffect(() => {
    if (currentUser && currentUser.id) {
      fetch(`${API_URL}/api/users/${currentUser.id}`)
        .then(res => res.json())
        .then(data => {
          setLiveUser(data);
          setIsLoading(false);
        })
        .catch(err => {
          console.error("Failed to fetch live stats", err);
          setIsLoading(false);
        });
    }
  }, [currentUser]);

  if (isLoading) return <div className="text-white text-center mt-10 font-bold">Loading Live Stats...</div>;
  if (!liveUser) return <div className="text-white text-center mt-10">Please log in.</div>;

  
  const played = liveUser.duelsPlayed || 0;
  const won = liveUser.duelsWon || 0;
  const lost = played - won;
  const winRate = played > 0 ? Math.round((won / played) * 100) : 0;

  return (
    <div className="flex justify-center items-center h-[75vh] p-8 animate-in fade-in zoom-in duration-300">
      <div className="bg-gray-900 border border-gray-700 rounded-2xl p-8 w-full max-w-2xl shadow-2xl text-white">
        
        <div className="flex items-center gap-6 border-b border-gray-700 pb-6 mb-6">
          {liveUser.picture ? (
            <img src={liveUser.picture} alt="Profile" className="h-24 w-24 rounded-full border-4 border-purple-500 shadow-[0_0_20px_rgba(168,85,247,0.5)]" />
          ) : (
            <div className="bg-purple-600 h-24 w-24 rounded-full flex items-center justify-center text-4xl font-black shadow-[0_0_20px_rgba(168,85,247,0.5)]">
              {liveUser.username ? liveUser.username.charAt(0).toUpperCase() : 'U'}
            </div>
          )}
          
          <div>
            <h1 className="text-4xl font-black tracking-wider text-purple-400">{liveUser.username}</h1>
            <p className="text-gray-400 text-lg flex items-center gap-2 mt-1">
              <Trophy size={20} className="text-yellow-400" /> 
              <span className="font-bold text-white">{liveUser.points || 100}</span> Elo Points
            </p>
          </div>
        </div>

        <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
          <div className="bg-gray-800 p-4 rounded-xl border border-gray-700 text-center flex flex-col items-center">
            <Swords size={28} className="text-blue-400 mb-2" />
            <p className="text-gray-400 text-sm font-semibold uppercase">Duels Fought</p>
            <p className="text-3xl font-black text-white">{played}</p>
          </div>
          <div className="bg-gray-800 p-4 rounded-xl border border-gray-700 text-center flex flex-col items-center">
            <Target size={28} className="text-green-400 mb-2" />
            <p className="text-gray-400 text-sm font-semibold uppercase">Wins</p>
            <p className="text-3xl font-black text-green-400">{won}</p>
          </div>
          <div className="bg-gray-800 p-4 rounded-xl border border-gray-700 text-center flex flex-col items-center">
            <Activity size={28} className="text-red-400 mb-2" />
            <p className="text-gray-400 text-sm font-semibold uppercase">Losses</p>
            <p className="text-3xl font-black text-red-400">{lost}</p>
          </div>
          <div className="bg-gray-800 p-4 rounded-xl border border-gray-700 text-center flex flex-col items-center">
            <div className="text-2xl mb-2 font-black text-purple-400">%</div>
            <p className="text-gray-400 text-sm font-semibold uppercase">Win Rate</p>
            <p className="text-3xl font-black text-white">{winRate}%</p>
          </div>
        </div>

      </div>
    </div>
  );
}