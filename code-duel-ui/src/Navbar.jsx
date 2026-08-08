import { LogOut } from 'lucide-react';
import { GoogleLogin } from '@react-oauth/google';

export default function Navbar({ currentUser, onLoginSuccess, onLogout, activePage, setActivePage }) {
  const navLinks = [
    { id: 'duel', label: '1v1 Duel' },
    { id: 'history', label: 'My History' },
    { id: 'leaderboard', label: 'Leaderboard' },
    { id: 'profile', label: 'Profile' }
  ];

  return (
    <nav className="bg-[#16171d] border-b border-gray-800/50 relative z-50">
      <div className="max-w-7xl mx-auto px-6 py-4 flex items-center justify-between">
        
       
        <div 
          className="flex items-center gap-3 cursor-pointer select-none" 
          onClick={() => setActivePage('home')}
        >
          <div className="w-10 h-10 rounded-full bg-gradient-to-br from-purple-700 to-indigo-900 flex items-center justify-center border border-purple-500/30">
            <span className="text-white font-black text-sm tracking-tighter">{"</>"}</span>
          </div>
          <span className="text-white text-xl font-bold tracking-wide">
            CODE DUEL ARENA
          </span>
        </div>

       
        <div className="hidden md:flex items-center gap-8">
          {navLinks.map((link) => (
            <button 
              key={link.id}
              onClick={() => setActivePage(link.id)}
              className={`text-sm font-semibold transition-colors cursor-pointer ${
                activePage === link.id 
                  ? 'text-purple-400 font-bold border-b-2 border-purple-500 pb-1' 
                  : 'text-gray-400 hover:text-white'
              }`}
            >
              {link.label}
            </button>
          ))}
        </div>

       
        <div className="flex items-center gap-5">
          
          {currentUser ? (
            <div className="flex items-center gap-3 bg-[#1e2029] px-3 py-1.5 rounded-full border border-gray-700">
              {currentUser.picture ? (
                <img src={currentUser.picture} alt="Profile" className="w-7 h-7 rounded-full" />
              ) : (
                <div className="w-7 h-7 rounded-full bg-purple-600 flex items-center justify-center text-white font-bold text-xs">
                  {currentUser.username ? currentUser.username.charAt(0).toUpperCase() : 'U'}
                </div>
              )}
              <span className="text-white text-sm font-medium">{currentUser.username}</span>
              <button 
                onClick={onLogout} 
                title="Logout"
                className="text-gray-400 hover:text-red-400 ml-1 transition-colors cursor-pointer"
              >
                <LogOut size={16} />
              </button>
            </div>
          ) : (
            <div className="scale-90 origin-right">
              <GoogleLogin 
                onSuccess={onLoginSuccess} 
                onError={() => console.log('Login Failed')} 
                theme="filled_black"
                shape="pill"
              />
            </div>
          )}

        </div>

      </div>
    </nav>
  );
}