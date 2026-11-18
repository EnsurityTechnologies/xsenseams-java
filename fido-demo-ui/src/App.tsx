import React, { useState } from 'react';
import { demoApi, LoginStartResponse } from './api/demoApi';
import { base64urlToUint8Array, bufferToBase64url } from './utils/webauthnBase64';
import './App.css';

type AssertionPublicKey = NonNullable<LoginStartResponse['credential_assertion']['publicKey']>;

function optionalConfigId(value: string): string | undefined {
  const id = value.trim();
  return id.length > 0 ? id : undefined;
}

function toCreationOptions(options: {
  challenge: string;
  user: { id: string; name: string; displayName: string };
  pubKeyCredParams?: Array<{ type: string; alg: number }>;
  [key: string]: unknown;
}): PublicKeyCredentialCreationOptions {
  return {
    ...options,
    challenge: base64urlToUint8Array(options.challenge) as BufferSource,
    user: {
      ...options.user,
      id: base64urlToUint8Array(options.user.id) as BufferSource,
    },
    pubKeyCredParams: options.pubKeyCredParams || [],
  } as PublicKeyCredentialCreationOptions;
}

function toRequestOptions(options: AssertionPublicKey): PublicKeyCredentialRequestOptions {
  const { allowCredentials, challenge, ...rest } = options;
  const publicKey = {
    ...rest,
    challenge: base64urlToUint8Array(challenge) as BufferSource,
  } as PublicKeyCredentialRequestOptions;
  if (allowCredentials && allowCredentials.length > 0) {
    publicKey.allowCredentials = allowCredentials.map((cred) => ({
      type: 'public-key',
      id: base64urlToUint8Array(cred.id) as BufferSource,
    }));
  }
  return publicKey;
}

async function getAssertion(credentialAssertion: LoginStartResponse['credential_assertion']) {
  const options = credentialAssertion?.publicKey;
  if (!options) {
    throw new Error('Server did not return credential assertion options.');
  }
  const assertion = (await navigator.credentials.get({
    publicKey: toRequestOptions(options),
  })) as PublicKeyCredential | null;
  if (!assertion) {
    throw new Error('Assertion was cancelled or failed.');
  }
  const raw = assertion as unknown as {
    id: string;
    rawId: ArrayBuffer;
    response: {
      authenticatorData: ArrayBuffer;
      clientDataJSON: ArrayBuffer;
      signature: ArrayBuffer;
      userHandle: ArrayBuffer | null;
    };
  };
  return {
    id: raw.id,
    rawId: bufferToBase64url(raw.rawId),
    type: 'public-key' as const,
    response: {
      authenticatorData: bufferToBase64url(raw.response.authenticatorData),
      clientDataJSON: bufferToBase64url(raw.response.clientDataJSON),
      signature: bufferToBase64url(raw.response.signature),
      userHandle: raw.response.userHandle ? bufferToBase64url(raw.response.userHandle) : null,
    },
  };
}

function App() {
  const [registerUsername, setRegisterUsername] = useState('');
  const [loginUsername, setLoginUsername] = useState('');
  const [fidoServerConfigId, setFidoServerConfigId] = useState('');
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
      const startResp = await demoApi.registerStart(
        registerUsername.trim(),
        optionalConfigId(fidoServerConfigId),
      );
      const options = startResp.credential_creation?.publicKey;
      if (!options) {
        setError('Server did not return credential creation options.');
        return;
      }
      const credential = (await navigator.credentials.create({
        publicKey: toCreationOptions(options),
      })) as PublicKeyCredential | null;
      if (!credential) {
        setError('Credential creation was cancelled or failed.');
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
      const finishResp = await demoApi.registerFinish({
        session_id: startResp.session_id,
        credential_creation_response: {
          id: rawCred.id,
          rawId: bufferToBase64url(rawCred.rawId),
          type: 'public-key',
          response: {
            clientDataJSON: bufferToBase64url(rawCred.response.clientDataJSON),
            attestationObject: bufferToBase64url(rawCred.response.attestationObject),
          },
        },
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

  const handleUsernameLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    clearMessages();
    if (!loginUsername.trim()) {
      setError('Enter a username for username login.');
      return;
    }
    setLoading(true);
    try {
      const startResp = await demoApi.loginStart(
        loginUsername.trim(),
        optionalConfigId(fidoServerConfigId),
      );
      const assertion = await getAssertion(startResp.credential_assertion);
      const finishResp = await demoApi.loginFinish({
        session_id: startResp.session_id,
        credential_assertion_response: assertion,
      });
      if (finishResp.status) {
        setLoginStatus(finishResp.message || 'FIDO login successful.');
      } else {
        setError(finishResp.message || 'Login failed.');
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Login failed.');
    } finally {
      setLoading(false);
    }
  };

  const handleSecurityKeyLogin = async () => {
    clearMessages();
    setLoading(true);
    try {
      const startResp = await demoApi.loginInit(optionalConfigId(fidoServerConfigId));
      const assertion = await getAssertion(startResp.credential_assertion);
      const finishResp = await demoApi.loginInitFinish({
        session_id: startResp.session_id,
        credential_assertion_response: assertion,
      });
      if (finishResp.status) {
        setLoginStatus(
          finishResp.username
            ? `FIDO login successful for ${finishResp.username}.`
            : 'FIDO login successful.',
        );
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
        <p>Register a security key, then sign in with a username or with the key alone.</p>
      </header>

      <main className="main">
        {error && <div className="message error">{error}</div>}
        {registerStatus && <div className="message success">{registerStatus}</div>}
        {loginStatus && <div className="message success">{loginStatus}</div>}

        <section className="card">
          <h2>FIDO server</h2>
          <label>
            Config id
            <input
              type="text"
              value={fidoServerConfigId}
              onChange={(e) => setFidoServerConfigId(e.target.value)}
              placeholder="optional"
              disabled={loading}
            />
          </label>
          <p className="hint">
            Leave this blank to use the Java backend setting ams.fido-server-config-id.
            Security-key login does not ask for a username, so that backend id is what selects the configuration.
            A value typed here overrides the backend for this ceremony. This page origin must be listed on that configuration.
          </p>
        </section>

        <section className="card">
          <h2>Register FIDO</h2>
          <form onSubmit={handleRegister}>
            <label>
              Username
              <input
                type="text"
                value={registerUsername}
                onChange={(e) => setRegisterUsername(e.target.value)}
                placeholder="username or email"
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
          <form onSubmit={handleUsernameLogin}>
            <label>
              Username
              <input
                type="text"
                value={loginUsername}
                onChange={(e) => setLoginUsername(e.target.value)}
                placeholder="username or email"
                disabled={loading}
              />
            </label>
            <div className="actions">
              <button type="submit" disabled={loading}>
                {loading ? 'Please wait…' : 'Login with username'}
              </button>
              <button
                type="button"
                className="secondary"
                onClick={handleSecurityKeyLogin}
                disabled={loading}
              >
                {loading ? 'Please wait…' : 'Login with security key'}
              </button>
            </div>
          </form>
          <p className="hint">
            Username login uses that user&apos;s active FIDO credentials. Security-key login does not send a username.
            The key must be a resident credential registered here.
          </p>
        </section>
      </main>

      <footer className="footer">
        <p>
          AMS must be running, this UI origin must be on the selected FIDO configuration, and the sample
          Java backend must be started (port 8080). Set VITE_API_BASE if the backend is elsewhere.
        </p>
      </footer>
    </div>
  );
}

export default App;
