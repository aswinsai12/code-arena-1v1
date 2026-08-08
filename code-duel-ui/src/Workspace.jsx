import { useState, useEffect, useCallback } from 'react';
import { Play, Send, CheckCircle2, XCircle, AlertTriangle, LogOut } from 'lucide-react';
import Editor from '@monaco-editor/react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

export default function Workspace({ problemId, roomId, opponentId, currentUser, setActivePage }) {
  const [problem, setProblem] = useState(null);
  const [code, setCode] = useState(`import java.util.*;\n\nclass Main {\n    public static void main(String[] args) {\n        Scanner s = new Scanner(System.in);\n        int n = s.nextInt();\n        int[] arr = new int[n];\n        for(int i=0; i<n; i++) {\n            arr[i] = s.nextInt();\n        }\n        System.out.println("Read " + n + " elements!");\n    }\n}`);
  const [isRunning, setIsRunning] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [consoleOutput, setConsoleOutput] = useState("");
  const [submitResult, setSubmitResult] = useState(null); 
  const [customInput, setCustomInput] = useState("5\n3 2 5 1 7"); 
  const [matchResult, setMatchResult] = useState(null); 
  const [activeLeftTab, setActiveLeftTab] = useState('description');
  const [activeConsoleTab, setActiveConsoleTab] = useState('custom'); 

  const [leftWidth, setLeftWidth] = useState(50); 
  const [isDragging, setIsDragging] = useState(false);
  const [showExitModal, setShowExitModal] = useState(false); 
  const API_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080"
  useEffect(() => {
    const handleBeforeUnload = (e) => {
      e.preventDefault();
      e.returnValue = "If you leave, you will forfeit the match and lose points. Are you sure?";
      return e.returnValue;
    };
    window.addEventListener('beforeunload', handleBeforeUnload);
    return () => window.removeEventListener('beforeunload', handleBeforeUnload);
  }, []);

  const handleMouseMove = useCallback((e) => {
    if (!isDragging) return;
    const newWidth = (e.clientX / window.innerWidth) * 100;
    if (newWidth > 20 && newWidth < 80) setLeftWidth(newWidth);
  }, [isDragging]);

  const handleMouseUp = useCallback(() => setIsDragging(false), []);

  useEffect(() => {
    if (isDragging) {
      document.addEventListener('mousemove', handleMouseMove);
      document.addEventListener('mouseup', handleMouseUp);
    } else {
      document.removeEventListener('mousemove', handleMouseMove);
      document.removeEventListener('mouseup', handleMouseUp);
    }
    return () => {
      document.removeEventListener('mousemove', handleMouseMove);
      document.removeEventListener('mouseup', handleMouseUp);
    };
  }, [isDragging, handleMouseMove, handleMouseUp]);

  useEffect(() => {
    if (problemId) {
      fetch(`${API_URL}/api/problems/${problemId}`)
        .then(res => res.json())
        .then(data => setProblem(data))
        .catch(err => console.error("Error fetching problem:", err));
    }
  }, [problemId]);

  useEffect(() => {
    if (!currentUser) return;
    const activeRoom = roomId || "default-arena"; 

    const socket = new SockJS(`${API_URL}/ws`);
    const client = new Client({
      webSocketFactory: () => socket,
      onConnect: () => {
        client.subscribe(`/topic/duel/${activeRoom}`, (message) => {
          const data = JSON.parse(message.body);
          
          if (data.type === 'MATCH_OVER') {
        
            if (data.loserId === currentUser.id) {
                setMatchResult('LOST');
            } 
            
            else if (data.winnerId === currentUser.id) {
                setMatchResult(data.reason === 'FORFEIT' ? 'WON_FORFEIT' : 'WON');
            } 
     
            else {
                if (data.reason === 'FORFEIT') {
              
                    setMatchResult('WON_FORFEIT');
                } else {
                  
                    setMatchResult('LOST');
                }
            }
      
            setTimeout(() => {
              if(setActivePage) setActivePage('home'); 
            }, 4000);
          }
        });
      }
    });

    client.activate();
    return () => client.deactivate();
  }, [roomId, currentUser, setActivePage]);

  
  const handleForfeit = async () => {
    setShowExitModal(false);
    
    
    setMatchResult('LOST');
    setTimeout(() => {
      if(setActivePage) setActivePage('home'); 
    }, 4000);

    try {
      await fetch(`${API_URL}/api/execute/forfeit`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          roomId: roomId || "default-arena",
          userId: currentUser?.id,
          opponentId: opponentId || 9999 
        })
      });
    } catch (err) {
      console.error(err);
    }
  };

  const decodeHTML = (html) => {
    if (!html) return "";
    const txt = document.createElement("textarea");
    txt.innerHTML = html;
    return txt.value;
  };

  const handleRunCode = async () => {
    setIsRunning(true);
    setSubmitResult(null);
    setActiveConsoleTab('custom');
    setConsoleOutput("Compiling and running...");
    
    try {
      const response = await fetch(`${API_URL}/api/execute/run`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ code: code, input: customInput.trim() || "5\n3 2 5 1 7" }) 
      });
      const data = await response.text();
      setConsoleOutput(data);
    } catch (error) {
      setConsoleOutput("Server Error connecting to executor.");
    } finally {
      setIsRunning(false);
    }
  };

  const handleSubmitCode = async () => {
    setIsSubmitting(true);
    setConsoleOutput("");
    setActiveConsoleTab('result');
    setSubmitResult({ verdict: "TESTING", message: "Evaluating against hidden test cases..." });

    try {
      const response = await fetch(`${API_URL}/api/execute/submit`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ 
          problemId: problem.id, 
          userId: currentUser.id, 
          code: code,
          roomId: roomId || "default-arena", 
          opponentId: opponentId || 9999 
        }) 
      });
      const data = await response.json();
      setSubmitResult(data);
    } catch (error) {
      setSubmitResult({ verdict: "SYSTEM_ERROR", message: "Failed to connect to the arena server." });
    } finally {
      setIsSubmitting(false);
    }
  };

  
  if (matchResult === 'WON') {
    return (
      <div className="flex flex-col items-center justify-center w-full h-screen bg-green-900/90 text-white z-[200] absolute inset-0 animate-in fade-in zoom-in duration-500">
        <h1 className="text-7xl font-black mb-4 drop-shadow-lg">🏆 YOU WON!</h1>
        <h2 className="text-4xl font-bold">+5 Points</h2>
        <p className="mt-6 text-xl text-green-200 animate-pulse">Returning to lobby...</p>
      </div>
    );
  }

  if (matchResult === 'WON_FORFEIT') {
    return (
      <div className="flex flex-col items-center justify-center w-full h-screen bg-green-900/90 text-white z-[200] absolute inset-0 animate-in fade-in zoom-in duration-500">
        <h1 className="text-7xl font-black mb-4 drop-shadow-lg">🏆 YOU WON!</h1>
        <h2 className="text-3xl font-bold text-yellow-400 mb-2">Opponent Fled The Arena</h2>
        <h2 className="text-4xl font-bold">+5 Points</h2>
        <p className="mt-6 text-xl text-green-200 animate-pulse">Returning to lobby...</p>
      </div>
    );
  }

  if (matchResult === 'LOST') {
    return (
      <div className="flex flex-col items-center justify-center w-full h-screen bg-red-900/90 text-white z-[200] absolute inset-0 animate-in fade-in zoom-in duration-500">
        <h1 className="text-7xl font-black mb-4 drop-shadow-lg">💀 YOU LOST.</h1>
        <h2 className="text-4xl font-bold">-3 Points</h2>
        <p className="mt-6 text-xl text-red-200 animate-pulse">Returning to lobby...</p>
      </div>
    );
  }

  if (!problem) return <div className="text-white p-10 text-center w-full">Loading Arena...</div>;

  return (
    <div className={`flex flex-row w-full h-screen bg-[#12141d] font-sans text-left relative ${isDragging ? 'select-none pointer-events-none' : ''}`}>
      
      {showExitModal && (
        <div className="absolute inset-0 z-[100] flex items-center justify-center bg-black/80">
          <div className="bg-[#1e2029] border border-red-500/50 p-8 rounded-xl max-w-md text-center shadow-[0_0_50px_rgba(239,68,68,0.3)]">
            <AlertTriangle size={60} className="text-red-500 mx-auto mb-4" />
            <h2 className="text-3xl font-black text-white mb-2">Forfeit Match?</h2>
            <p className="text-gray-400 mb-8 font-medium">If you exit now, you will automatically lose and points will be deducted. The other player will instantly win.</p>
            <div className="flex gap-4 justify-center">
              <button onClick={() => setShowExitModal(false)} className="px-6 py-3 rounded-lg font-bold text-white bg-gray-700 hover:bg-gray-600 transition-colors w-1/2 cursor-pointer">
                Cancel
              </button>
              <button onClick={handleForfeit} className="px-6 py-3 rounded-lg font-bold text-white bg-red-600 hover:bg-red-500 transition-colors w-1/2 cursor-pointer">
                Yes, Forfeit
              </button>
            </div>
          </div>
        </div>
      )}

      <div style={{ width: `${leftWidth}%` }} className="h-full p-2 flex flex-col pointer-events-auto">
        <div className="h-full bg-[#1e2029] rounded-xl flex flex-col overflow-hidden shadow-lg border border-gray-800">
          <div className="flex justify-between items-center px-4 py-3 bg-[#16171d] border-b border-gray-800">
            <button onClick={() => setActiveLeftTab('description')} className={`text-sm font-semibold pb-1 transition-colors ${activeLeftTab === 'description' ? 'text-purple-400 border-b-2 border-purple-400' : 'text-gray-400 hover:text-white'}`}>
              Description
            </button>
            <button onClick={() => setShowExitModal(true)} className="flex items-center gap-2 px-3 py-1.5 bg-red-900/30 text-red-400 hover:bg-red-900/70 rounded-md transition-colors font-bold text-xs uppercase tracking-wider border border-red-500/30 cursor-pointer">
              <LogOut size={14} /> Exit Duel
            </button>
          </div>
          <div className="p-6 overflow-y-auto flex-grow text-[#d4d4d4]">
            {activeLeftTab === 'description' && (
              <>
                <h1 className="text-3xl font-black text-white m-0 mb-4">{problem.id}. {problem.title}</h1>
                <div className="flex gap-3 mb-6">
                  <span className="bg-purple-900/40 text-purple-400 border border-purple-500/30 px-3 py-1 rounded-full text-xs font-bold uppercase tracking-wider">Medium</span>
                  <span className="bg-gray-800 text-gray-300 px-3 py-1 rounded-full text-xs font-medium">Time: {problem.timeLimit || "1.00"}s</span>
                </div>
                <div className="leetcode-problem-text text-sm leading-relaxed" dangerouslySetInnerHTML={{ __html: decodeHTML(problem.description) }} />
              </>
            )}
          </div>
        </div>
      </div>

      <div className="w-2 h-full cursor-col-resize hover:bg-purple-500/50 transition-colors flex items-center justify-center z-10 pointer-events-auto" onMouseDown={() => setIsDragging(true)}>
        <div className="h-12 w-1 bg-gray-600 rounded-full" />
      </div>

      <div style={{ width: `${100 - leftWidth}%` }} className="h-full p-2 flex flex-col gap-2 pointer-events-auto">
        <div className="flex-grow bg-[#1e2029] rounded-xl flex flex-col overflow-hidden shadow-lg border border-gray-800">
          <div className="flex justify-between items-center px-4 py-2 bg-[#16171d] border-b border-gray-800">
            <span className="text-sm font-semibold text-purple-400 bg-purple-900/20 px-3 py-1 rounded-md border border-purple-500/30">Java</span>
          </div>
          <div className="flex-grow pt-2">
            <Editor height="100%" defaultLanguage="java" theme="vs-dark" value={code} onChange={(value) => setCode(value)} options={{ minimap: { enabled: false }, fontSize: 14 }} />
          </div>
        </div>

        <div className="h-72 bg-[#1e2029] rounded-xl flex flex-col overflow-hidden shadow-lg border border-gray-800">
          <div className="flex justify-between items-center px-4 py-3 bg-[#16171d] border-b border-gray-800">
            <div className="flex gap-4 text-sm font-bold tracking-wider uppercase text-gray-400">
              <span onClick={() => setActiveConsoleTab('custom')} className={`cursor-pointer pb-1 ${activeConsoleTab === 'custom' ? 'text-white border-b-2 border-white' : 'hover:text-gray-200'}`}>Test Area</span>
              <span onClick={() => setActiveConsoleTab('result')} className={`cursor-pointer pb-1 ${activeConsoleTab === 'result' ? 'text-purple-400 border-b-2 border-purple-400' : 'hover:text-gray-200'}`}>Submit Result</span>
            </div>
            
            <div className="flex gap-3">
              <button onClick={handleRunCode} disabled={isRunning || isSubmitting} className="bg-gray-800 hover:bg-gray-700 text-gray-200 px-5 py-2 rounded-lg text-sm font-bold transition-all flex items-center gap-2 cursor-pointer">
                <Play size={16} /> {isRunning ? "Running..." : "Run"}
              </button>
              <button onClick={handleSubmitCode} disabled={isRunning || isSubmitting} className="bg-purple-600 hover:bg-purple-500 text-white px-6 py-2 rounded-lg text-sm font-bold uppercase tracking-wider transition-all flex items-center gap-2 shadow-[0_0_15px_rgba(168,85,247,0.4)] hover:shadow-[0_0_25px_rgba(168,85,247,0.6)] cursor-pointer">
                <Send size={16} /> {isSubmitting ? "Testing..." : "Submit"}
              </button>
            </div>
          </div>

          <div className="p-4 font-mono text-sm flex-grow overflow-y-auto text-gray-300">
            {activeConsoleTab === 'custom' && (
              <div className="flex h-full gap-4">
                <div className="flex flex-col w-1/2 h-full">
                  <span className="text-xs text-gray-500 font-bold mb-2 uppercase tracking-wider">Custom Input:</span>
                  <textarea 
                    className="w-full flex-grow bg-[#16171d] text-gray-300 p-3 rounded-lg border border-gray-800 focus:outline-none focus:border-purple-500 resize-none font-mono text-sm"
                    value={customInput}
                    onChange={(e) => setCustomInput(e.target.value)}
                  />
                </div>
                <div className="flex flex-col w-1/2 h-full">
                  <span className="text-xs text-gray-500 font-bold mb-2 uppercase tracking-wider">Output:</span>
                  <div className="w-full flex-grow bg-[#16171d] text-gray-300 p-3 rounded-lg border border-gray-800 overflow-y-auto font-mono text-sm whitespace-pre-wrap">
                    {consoleOutput || <span className="text-gray-600 italic">Output will appear here after running...</span>}
                  </div>
                </div>
              </div>
            )}

            {activeConsoleTab === 'result' && submitResult && (
              <div className="flex flex-col gap-4">
                {submitResult.verdict === 'TESTING' && <div className="text-blue-400 font-bold animate-pulse">{submitResult.message}</div>}
                
                {submitResult.verdict === 'ACCEPTED' && (
                  <div className="bg-green-900/20 border border-green-500/30 p-4 rounded-lg">
                    <h2 className="text-2xl font-black text-green-400 flex items-center gap-2 mb-2"><CheckCircle2 size={28}/> ACCEPTED</h2>
                    <p className="text-green-200/70">Your logic crushed all hidden test cases!</p>
                  </div>
                )}

                {submitResult.verdict === 'WRONG_ANSWER' && (
                  <div className="bg-red-900/20 border border-red-500/30 p-4 rounded-lg">
                    <h2 className="text-2xl font-black text-red-400 flex items-center gap-2 mb-4"><XCircle size={28}/> WRONG ANSWER</h2>
                    <div className="grid gap-3">
                      <div className="bg-[#16171d] p-3 rounded border border-gray-800">
                        <div className="text-xs text-gray-500 uppercase font-bold mb-1">Failed Input:</div>
                        <div className="text-gray-300 whitespace-pre-wrap">{submitResult.failedInput}</div>
                      </div>
                      <div className="grid grid-cols-2 gap-3">
                        <div className="bg-[#16171d] p-3 rounded border border-gray-800">
                          <div className="text-xs text-gray-500 uppercase font-bold mb-1">Expected Output:</div>
                          <div className="text-green-400 whitespace-pre-wrap">{submitResult.expectedOutput}</div>
                        </div>
                        <div className="bg-[#16171d] p-3 rounded border border-gray-800">
                          <div className="text-xs text-gray-500 uppercase font-bold mb-1">Your Output:</div>
                          <div className="text-red-400 whitespace-pre-wrap">{submitResult.actualOutput}</div>
                        </div>
                      </div>
                    </div>
                  </div>
                )}

                {submitResult.verdict === 'ERROR' && (
                  <div className="bg-yellow-900/20 border border-yellow-500/30 p-4 rounded-lg">
                    <h2 className="text-2xl font-black text-yellow-400 flex items-center gap-2 mb-2"><AlertTriangle size={28}/> EXECUTION ERROR</h2>
                    <pre className="text-yellow-200/70 whitespace-pre-wrap mt-2 bg-[#16171d] p-3 rounded">{submitResult.actualOutput}</pre>
                  </div>
                )}
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}