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
          rp: {
            ...options.rp,
            id: window.location.hostname,
          },
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
    setLoading(true);
    try {
      const startResp = await demoApi.loginInit();
      const initSessionId = startResp.session_id;
      const initCredentialAssertion = startResp.credential_assertion;
      const initOptions = initCredentialAssertion?.publicKey;
      if (!initOptions) {
        setError('Server did not return credential assertion options.');
        setLoading(false);
        return;
      }
      console.log('Received login init response:', initCredentialAssertion);

      const publicKey: PublicKeyCredentialRequestOptions = {
        ...initOptions,
        rpId: window.location.hostname,
        challenge: base64urlToUint8Array(initOptions.challenge) as BufferSource,
        allowCredentials: (initOptions.allowCredentials ?? []).map(
          (cred: { type?: string; id: string }) => ({
            type: "public-key",
            id: base64urlToUint8Array(cred.id) as BufferSource,
            // optional but recommended if you have it:
            // transports: cred.transports,
          }),
        ),
        // optional: keep what you already set in initOptions (rpId, timeout, userVerification, extensions...)
      };

      const requestOptions: CredentialRequestOptions = {
        publicKey,
        mediation: initCredentialAssertion.mediation as CredentialMediationRequirement | undefined,
        // optional: if you were using it
        // signal: abortController.signal,
      };

      console.log('Requesting login assertion with options:', requestOptions);
      
      const initAssertion = (await navigator.credentials.get(requestOptions)) as PublicKeyCredential | null;
      console.log('Received login init assertion:', initAssertion);
      if (!initAssertion) {
        setError('Assertion was cancelled or failed.');
        setLoading(false);
        return;
      }
      const rawInitAssert = initAssertion as unknown as {
        id: string;
        rawId: ArrayBuffer;
        response: {
          authenticatorData: ArrayBuffer;
          clientDataJSON: ArrayBuffer;
          signature: ArrayBuffer;
          userHandle: ArrayBuffer | null;
        };
      };
      const credentialInitAssertionResponse = {
        id: rawInitAssert.id,
        rawId: bufferToBase64url(rawInitAssert.rawId),
        type: 'public-key' as const,
        response: {
          authenticatorData: bufferToBase64url(rawInitAssert.response.authenticatorData),
          clientDataJSON: bufferToBase64url(rawInitAssert.response.clientDataJSON),
          signature: bufferToBase64url(rawInitAssert.response.signature),
          userHandle: rawInitAssert.response.userHandle
            ? bufferToBase64url(rawInitAssert.response.userHandle)
            : null,
        },
      };
      const finishInitResp = await demoApi.loginInitFinish({
        session_id: initSessionId,
        credential_assertion_response: credentialInitAssertionResponse,
      });

      
      if (finishInitResp.status) {
        setLoginStatus('FIDO login successful for user: ' + finishInitResp.username);
      } else {
        setError(finishInitResp.message || 'Login failed.');
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
