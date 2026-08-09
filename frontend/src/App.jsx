import { useState,useEffect } from 'react';
import Navbar from "./Navbar";
import Workspace from './Workspace';
import History from './History';
import Leaderboard from './Leaderboard';
import DuelLobby from './DuelLobby';
import Profile from './Profile';
import { Swords } from 'lucide-react';
import './App.css'; 

function App() {
  
  const [currentUser, setCurrentUser] = useState(() => {
    const saved = localStorage.getItem("user");
    return saved ? JSON.parse(saved) : null;
  });
  const API_URL = import.meta.env.VITE_API_BASE_URL ||"http://localhost:8080";

  const [activePage, setActivePage] = useState('home'); 
  const [activeProblemId, setActiveProblemId] = useState(1068); 
  const [activeRoomId, setActiveRoomId] = useState(null); 
  const [activeOpponentId, setActiveOpponentId] = useState(null);
  useEffect(() => {
    
    if (currentUser?.id && activePage !== 'workspace') {
      fetch(`${API_URL}/api/users/${currentUser.id}`)
        .then(res => res.json())
        .then(freshData => {
          
          const updatedUser = { ...freshData, picture: currentUser.picture };
          setCurrentUser(updatedUser);
          localStorage.setItem("user", JSON.stringify(updatedUser));
        })
        .catch(err => console.error("Failed to refresh live stats:", err));
    }
  }, [activePage]);
  
  const decodeJWT = (token) => {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(atob(base64).split('').map(c => 
      '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)
    ).join(''));
    return JSON.parse(jsonPayload);
  };

  const handleGoogleSuccess = async (credentialResponse) => {
    try {
      const decodedInfo = decodeJWT(credentialResponse.credential);
      const response = await fetch(`${API_URL}/api/users/auth`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email: decodedInfo.email, username: decodedInfo.name })
      });
      if (!response.ok) throw new Error("Auth Failed");
      
      const userData = await response.json();
      const fullUser = { ...userData, picture: decodedInfo.picture };
      setCurrentUser(fullUser); 
      localStorage.setItem("user", JSON.stringify(fullUser));
    } catch (error) {
      console.error("Authentication failed:", error);
    }
  };

  const handleAppLogout = () => {
    setCurrentUser(null);
    localStorage.removeItem("user");
    setActivePage('home');
  };

  
  const handleMatchStart = (problemId, roomId, opponentId) => {
    setActiveProblemId(problemId);
    setActiveRoomId(roomId); 
    setActiveOpponentId(opponentId); 
    setActivePage('workspace');
  };

  
  return (
    <div className="min-h-screen flex flex-col font-sans bg-[#12141d] relative overflow-hidden">
      <div className="absolute top-0 left-1/4 w-[500px] h-[500px] bg-purple-900/20 rounded-full blur-[120px] pointer-events-none"></div>
      <div className="absolute bottom-0 right-1/4 w-[500px] h-[500px] bg-teal-900/10 rounded-full blur-[120px] pointer-events-none"></div>
      {activePage !== 'workspace' && (
        <Navbar 
          currentUser={currentUser} 
          onLoginSuccess={handleGoogleSuccess} 
          onLogout={handleAppLogout}
          activePage={activePage}
          setActivePage={setActivePage}
        />
      )}
      {activePage === 'home' && (
        <div className="flex-1 flex flex-col items-center justify-center p-6 z-10 relative">
          
          <p className="text-gray-400 font-bold uppercase tracking-[0.3em] mb-8 text-sm">
            PROVE YOUR LOGIC. CRUSH YOUR OPPONENTS.
          </p>
          <br />
          <button 
            onClick={() => setActivePage('duel')} 
            className="group flex flex-col items-center justify-center bg-gradient-to-b from-[#1c162c] to-[#12111a] border-2 border-purple-500/40 hover:border-purple-400 p-12 w-full max-w-md rounded-2xl transition-all duration-300 hover:scale-105 shadow-[0_0_30px_rgba(168,85,247,0.15)] hover:shadow-[0_0_50px_rgba(168,85,247,0.3)] cursor-pointer"
          >
            <div className="relative mb-6">
              <div className="absolute inset-0 bg-purple-500 blur-xl opacity-20 group-hover:opacity-40 transition-opacity"></div>
              <Swords size={80} className="text-purple-400 relative z-10 drop-shadow-lg" />
            </div>
            <h2 className="text-3xl font-black mb-2 text-white tracking-wide">Ranked 1v1</h2>
            <span className="text-gray-400 text-sm font-medium">Matchmake against a live opponent.</span>
          </button>

        </div>
      )}
      {activePage === 'workspace' && (
        <Workspace 
          problemId={activeProblemId} 
          roomId={activeRoomId} 
          currentUser={currentUser} 
          opponentId={activeOpponentId}
          setActivePage={setActivePage} 
        />
      )}
      
      {activePage === 'history' && <History currentUser={currentUser} />}
      {activePage === 'leaderboard' && <Leaderboard />}
      {activePage === 'duel' && <DuelLobby currentUser={currentUser} onMatchStart={handleMatchStart} />}
      {activePage === 'profile' && <Profile currentUser={currentUser} />}
      
    </div>
  );
}

export default App;