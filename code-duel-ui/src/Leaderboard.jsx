import { useState, useEffect } from 'react';
import { Trophy, Medal } from 'lucide-react';

export default function Leaderboard() {
  const [leaders, setLeaders] = useState([]);
  const [loading, setLoading] = useState(true);
const API_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";
  useEffect(() => {
    fetch(`${API_URL}/api/users/leaderboard`)
      .then(res => res.json())
      .then(data => {
        setLeaders(data);
        setLoading(false);
      })
      .catch(err => {
        console.error("Error fetching leaderboard:", err);
        setLoading(false);
      });
  }, []);

  return (
    <div className="p-6 max-w-4xl mx-auto text-white mt-8">
      <div className="flex items-center gap-4 mb-8 justify-center">
        <Trophy className="text-yellow-500" size={40} />
        <h2 className="text-4xl font-extrabold tracking-tight">Global Rankings</h2>
      </div>
      
      <div className="bg-[#1e293b] rounded-xl border border-gray-700 shadow-2xl overflow-hidden">
        {loading ? (
          <div className="p-10 text-center animate-pulse text-gray-400">Loading top duelists...</div>
        ) : (
          <div className="flex flex-col">
            {leaders.map((user, index) => (
              <div 
                key={index} 
                className={`flex justify-between items-center p-5 border-b border-gray-800 transition-colors ${index < 3 ? 'bg-[#25324a]' : 'hover:bg-[#26334a]'}`}
              >
                <div className="flex items-center gap-5">
                  <span className={`text-2xl font-black w-10 text-center ${
                    index === 0 ? 'text-yellow-500' : 
                    index === 1 ? 'text-gray-300' : 
                    index === 2 ? 'text-amber-600' : 'text-gray-600'
                  }`}>
                    #{index + 1}
                  </span>
                  
                 
                  <div className="flex items-center gap-3">
                    <div className="w-10 h-10 rounded-full bg-blue-600 flex items-center justify-center font-bold shadow-inner">
                      {user.username?.charAt(0) || "U"}
                    </div>
                    <span className="font-bold text-xl">{user.username || "Unknown"}</span>
                  </div>
                </div>

               
                <div className="flex items-center gap-2 bg-[#0f172a] px-4 py-2 rounded-lg border border-gray-700">
                  <Medal size={16} className="text-green-400" />
                  <span className="text-green-400 font-bold">{user.rating || 0} Rating</span>
                </div>
              </div>
            ))}
            {leaders.length === 0 && !loading && (
              <div className="p-10 text-center text-gray-500">The arena is empty. Claim the #1 spot!</div>
            )}
          </div>
        )}
      </div>
    </div>
  );
}