import React, { useState } from 'react';
import axios from 'axios'; // Make sure you have installed axios: npm install axios
import { useNavigate } from 'react-router-dom'; // From react-router-dom

const LoginPage: React.FC = () => {
  const [username, setUsername] = useState<string>('');
  const [password, setPassword] = useState<string>('');
  const [error, setError] = useState<string | null>(null); // State for error messages
  const navigate = useNavigate(); // Hook for programmatic navigation

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault(); // Prevent default form submission behavior (page reload)
    setError(null); // Clear previous errors

    try {
      // Make the POST request to your backend's login endpoint
      const response = await axios.post('http://localhost:8080/api/auth/login', {
        username,
        password,
      });

      // Destructure the response data to match the backend's LoginResponse DTO
      // Ensure your backend's LoginResponse DTO returns 'token', 'userId', 'username', and 'role'
      const { token, userId, username: returnedUsername, role } = response.data;

      // Store authentication and user information in localStorage for persistence
      localStorage.setItem('jwtToken', token);
      localStorage.setItem('userRoles', JSON.stringify([role])); // Store the single role as an array for consistency
      localStorage.setItem('userId', userId.toString()); // Convert userId to string for localStorage
      localStorage.setItem('username', returnedUsername); // Store the username

      // Dispatch a custom event to notify other parts of the app (like App.tsx) about login
      // This is particularly useful for the 'storage' event listener in App.tsx
      window.dispatchEvent(new Event('storage'));

      // Redirect to the home page or a dashboard after successful login
      navigate('/');

      console.log('Login successful!', response.data);
      alert('Login Successful!'); // Simple alert for immediate user feedback

    } catch (err) {
      console.error('Login failed:', err);
      if (axios.isAxiosError(err) && err.response) {
        // If it's an Axios error and the response exists, try to get a specific message from backend
        // Your backend might send a 'message' field in its error response, e.g., for 401 Unauthorized
        setError(err.response.data.message || 'Login failed. Please check your credentials.');
      } else {
        // Generic error message for network issues or unexpected errors
        setError('An unexpected error occurred during login.');
      }
    }
  };

  return (
    <div style={{ maxWidth: '400px', margin: '50px auto', padding: '20px', border: '1px solid #ccc', borderRadius: '8px', boxShadow: '0 2px 4px rgba(0,0,0,0.1)' }}>
      <h2>Login to Your Account</h2>
      <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '15px' }}>
        <div>
          <label htmlFor="username" style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold', textAlign: 'left' }}>Username:</label>
          <input
            type="text"
            id="username"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            required
            style={{ width: '100%', padding: '10px', border: '1px solid #ddd', borderRadius: '4px', boxSizing: 'border-box' }}
          />
        </div>
        <div>
          <label htmlFor="password" style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold', textAlign: 'left' }}>Password:</label>
          <input
            type="password"
            id="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            style={{ width: '100%', padding: '10px', border: '1px solid #ddd', borderRadius: '4px', boxSizing: 'border-box' }}
          />
        </div>
        {error && <p style={{ color: 'red', fontSize: '0.9em' }}>{error}</p>} {/* Display error message */}
        <button type="submit" style={{ padding: '10px 15px', backgroundColor: '#007bff', color: 'white', border: 'none', borderRadius: '5px', cursor: 'pointer', fontSize: '1em' }}>
          Login
        </button>
      </form>
    </div>
  );
};

export default LoginPage;