import { useState, useEffect } from 'react';

export default function History({ currentUser }) {
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(true);
 const API_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";
  useEffect(() => {
    if (currentUser) {
      fetch(`${API_URL}/api/submissions/history/${currentUser.id}`)
        .then(res => {
          if (!res.ok) throw new Error("Failed to fetch");
          return res.json();
        })
        .then(data => {
          setHistory(data);
          setLoading(false);
        })
        .catch(err => {
          console.error("Error fetching history:", err);
          setLoading(false);
        });
    }
  }, [currentUser]);

  if (!currentUser) return <div className="text-white p-6 text-center mt-10">Please log in to view your battle history.</div>;

  return (
    <div className="p-6 max-w-4xl mx-auto text-white">
      <h2 className="text-2xl font-bold mb-6">Your Battle History</h2>
      <div className="bg-[#1e293b] rounded-lg overflow-hidden border border-gray-700 shadow-lg">
        
        <table className="w-full text-left">
          <thead className="bg-[#0f172a] border-b border-gray-700">
            <tr>
              <th className="p-4 text-gray-400 font-semibold tracking-wider uppercase text-sm">Challenge</th>
              <th className="p-4 text-gray-400 font-semibold tracking-wider uppercase text-sm">Verdict</th>
              <th className="p-4 text-gray-400 font-semibold tracking-wider uppercase text-sm">Execution Time</th>
            </tr>
          </thead>
          <tbody>
            {history.map((sub) => (
              <tr key={sub.id} className="border-b border-gray-800 hover:bg-[#26334a] transition-colors">
             
                <td className="p-4 font-medium text-gray-200">
                  {sub.problem ? sub.problem.title : `Task #${sub.id}`}
                </td>
                
                <td className={`p-4 font-bold ${sub.verdict === 'ACCEPTED' ? 'text-green-500' : sub.verdict.includes('PENDING') || sub.verdict.includes('RUNNING') ? 'text-yellow-500' : 'text-red-500'}`}>
                  {sub.verdict}
                </td>
                
                <td className="p-4 text-gray-400 font-mono">
                  {sub.execTime ? `${sub.execTime} s` : '--'}
                </td>
              </tr>
            ))}
          </tbody>
        </table>

        {loading && <div className="p-8 text-center text-gray-400 animate-pulse">Loading past battles...</div>}
        
        {!loading && history.length === 0 && (
          <div className="p-8 text-center text-gray-500">
            No submissions yet. Head to the Arena and make your first move!
          </div>
        )}

      </div>
    </div>
  );
}