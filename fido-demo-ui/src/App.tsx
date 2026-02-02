import React, { useState } from 'react';
import { demoApi } from './api/demoApi';
import { base64urlToUint8Array, bufferToBase64url } from './utils/webauthnBase64';
import './App.css';

function App() {
  const [registerUsername, setRegisterUsername] = useState('');
  const [loginUsername, setLoginUsername] = useState('');
  const [registerStatus, setRegisterStatus] = useState<string | null>(null);
  const [loginStatus, setLoginStatus] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const clearMessages = () => {
    setRegisterStatus(null);
    setLoginStatus(null);
    setError(null);
  };

  const handleRegister = async (e: React.FormEvent) => {
    e.preventDefault();
    clearMessages();
    if (!registerUsername.trim()) {
      setError('Enter a username for registration.');
      return;
    }
    setLoading(true);
    try {
      const startResp = await demoApi.registerStart(registerUsername.trim());
      const sessionId = startResp.session_id;
      const credentialCreation = startResp.credential_creation;
      const options = credentialCreation?.publicKey;
      if (!options) {
        setError('Server did not return credential creation options.');
        setLoading(false);
        return;
      }
      const credential = (await navigator.credentials.create({
        publicKey: {
          ...options,
          challenge: base64urlToUint8Array(options.challenge) as BufferSource,
          user: {
            ...options.user,
            id: base64urlToUint8Array(options.user.id) as BufferSource,
          },
          pubKeyCredParams: options.pubKeyCredParams || [],
        } as PublicKeyCredentialCreationOptions,
      })) as PublicKeyCredential | null;
      if (!credential) {
        setError('Credential creation was cancelled or failed.');
        setLoading(false);
        return;
      }
      const rawCred = credential as unknown as {
        id: string;
        rawId: ArrayBuffer;
        response: {
          clientDataJSON: ArrayBuffer;
          attestationObject: ArrayBuffer;
        };
      };
      const credentialCreationResponse = {
        id: rawCred.id,
        rawId: bufferToBase64url(rawCred.rawId),
        type: 'public-key' as const,
        response: {
          clientDataJSON: bufferToBase64url(rawCred.response.clientDataJSON),
          attestationObject: bufferToBase64url(rawCred.response.attestationObject),
        },
      };
      const finishResp = await demoApi.registerFinish({
        username: registerUsername.trim(),
        session_id: sessionId,
        credential_creation_response: credentialCreationResponse,
      });
      if (finishResp.status) {
        setRegisterStatus('FIDO credential registered successfully.');
      } else {
        setError(finishResp.message || 'Registration failed.');
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Registration failed.');
    } finally {
      setLoading(false);
    }
  };

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    clearMessages();
    if (!loginUsername.trim()) {
      setError('Enter a username for login.');
      return;
    }
    setLoading(true);
    try {
      const startResp = await demoApi.loginStart(loginUsername.trim());
      const sessionId = startResp.session_id;
      const credentialAssertion = startResp.credential_assertion;
      const options = credentialAssertion?.publicKey;
      if (!options) {
        setError('Server did not return credential assertion options.');
        setLoading(false);
        return;
      }
      const assertion = (await navigator.credentials.get({
        publicKey: {
          ...options,
          challenge: base64urlToUint8Array(options.challenge) as BufferSource,
          allowCredentials: (options.allowCredentials || []).map((cred: { type: string; id: string }) => ({
            type: 'public-key' as const,
            id: base64urlToUint8Array(cred.id) as BufferSource,
          })),
        } as PublicKeyCredentialRequestOptions,
      })) as PublicKeyCredential | null;
      if (!assertion) {
        setError('Assertion was cancelled or failed.');
        setLoading(false);
        return;
      }
      const rawAssert = assertion as unknown as {
        id: string;
        rawId: ArrayBuffer;
        response: {
          authenticatorData: ArrayBuffer;
          clientDataJSON: ArrayBuffer;
          signature: ArrayBuffer;
          userHandle: ArrayBuffer | null;
        };
      };
      const credentialAssertionResponse = {
        id: rawAssert.id,
        rawId: bufferToBase64url(rawAssert.rawId),
        type: 'public-key' as const,
        response: {
          authenticatorData: bufferToBase64url(rawAssert.response.authenticatorData),
          clientDataJSON: bufferToBase64url(rawAssert.response.clientDataJSON),
          signature: bufferToBase64url(rawAssert.response.signature),
          userHandle: rawAssert.response.userHandle
            ? bufferToBase64url(rawAssert.response.userHandle)
            : null,
        },
      };
      const finishResp = await demoApi.loginFinish({
        username: loginUsername.trim(),
        session_id: sessionId,
        factor_index: 1,
        credential_assertion_response: credentialAssertionResponse,
      });
      if (finishResp.status) {
        setLoginStatus('FIDO login successful.');
      } else {
        setError(finishResp.message || 'Login failed.');
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Login failed.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="app">
      <header className="header">
        <h1>XSenseAMS FIDO Demo</h1>
        <p>Register and sign in with WebAuthn </p>
      </header>

      <main className="main">
        {error && <div className="message error">{error}</div>}
        {registerStatus && <div className="message success">{registerStatus}</div>}
        {loginStatus && <div className="message success">{loginStatus}</div>}

        <section className="card">
          <h2>Register FIDO</h2>
          <form onSubmit={handleRegister}>
            <label>
              Username
              <input
                type="text"
                value={registerUsername}
                onChange={(e) => setRegisterUsername(e.target.value)}
                placeholder="username"
                disabled={loading}
              />
            </label>
            <button type="submit" disabled={loading}>
              {loading ? 'Please wait…' : 'Register FIDO'}
            </button>
          </form>
        </section>

        <section className="card">
          <h2>Login with FIDO</h2>
          <form onSubmit={handleLogin}>
            <label>
              Username
              <input
                type="text"
                value={loginUsername}
                onChange={(e) => setLoginUsername(e.target.value)}
                placeholder="username"
                disabled={loading}
              />
            </label>
            <button type="submit" disabled={loading}>
              {loading ? 'Please wait…' : 'Login with FIDO'}
            </button>
          </form>
        </section>
      </main>

      <footer className="footer">
        <p>Ensure XSenseAMS is running, the sample Java backend is started (e.g. port 8080), and set VITE_API_BASE if needed.</p>
      </footer>
    </div>
  );
}

export default App;
