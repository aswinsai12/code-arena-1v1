import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { GoogleOAuthProvider } from '@react-oauth/google'
import App from './App.jsx'
import './index.css' 
const ID = import.meta.env.CLIENT_ID;
createRoot(document.getElementById('root')).render(
  <StrictMode>

    <GoogleOAuthProvider clientId={ID}>
      <App />
    </GoogleOAuthProvider>
  </StrictMode>,
)