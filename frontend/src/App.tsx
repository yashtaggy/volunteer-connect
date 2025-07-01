// frontend/src/App.tsx
import React, { useState, useEffect } from 'react';
import './App.css';

import { BrowserRouter as Router, Routes, Route, Link, useNavigate } from 'react-router-dom';

import Home from './pages/Home';
import LoginPage from './pages/LoginPage';
import DashboardPage from './pages/DashboardPage';
import EventsPage from './pages/EventsPage'; // This will now render the list of events
import UserProfilePage from './pages/UserProfilePage';
import CreateEventPage from './pages/CreateEventPage'; // <-- IMPORT THIS

const PrivateRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const isAuthenticated = localStorage.getItem('jwtToken');
  const navigate = useNavigate();

  useEffect(() => {
    if (!isAuthenticated) {
      navigate('/login');
    }
  }, [isAuthenticated, navigate]);

  return isAuthenticated ? <>{children}</> : null;
};

function App() {
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [currentUser, setCurrentUser] = useState<string | null>(null);
  const [currentUserRole, setCurrentUserRole] = useState<string | null>(null); // <-- NEW STATE FOR ROLE

  useEffect(() => {
    const updateLoginStatus = () => {
      const token = localStorage.getItem('jwtToken');
      const username = localStorage.getItem('username');
      const rolesString = localStorage.getItem('userRoles'); // This is a JSON string of roles

      if (token && username && rolesString) {
        setIsLoggedIn(true);
        setCurrentUser(username);
        try {
          const rolesArray = JSON.parse(rolesString);
          // Assuming the first role in the array is the primary one, or you can check all
          setCurrentUserRole(rolesArray[0]);
        } catch (e) {
          console.error("Failed to parse user roles from localStorage", e);
          setCurrentUserRole(null);
        }
      } else {
        setIsLoggedIn(false);
        setCurrentUser(null);
        setCurrentUserRole(null); // Clear role on logout
      }
    };

    updateLoginStatus();
    // Listen for changes in localStorage from other tabs/windows (e.g., login/logout)
    window.addEventListener('storage', updateLoginStatus);
    return () => {
      window.removeEventListener('storage', updateLoginStatus);
    };
  }, []);

  const handleLogout = () => {
    localStorage.removeItem('jwtToken');
    localStorage.removeItem('userRoles');
    localStorage.removeItem('userId');
    localStorage.removeItem('username');
    setIsLoggedIn(false);
    setCurrentUser(null);
    setCurrentUserRole(null); // Clear role on logout
    // Dispatch a storage event so other components/tabs listening will update
    window.dispatchEvent(new Event('storage'));
  };

  // Helper function to check if the user has a specific role
  const hasRole = (role: string) => {
    return isLoggedIn && currentUserRole === role;
  };

  return (
    <Router>
      <div className="App">
        <nav style={{ marginBottom: '20px', padding: '10px', backgroundColor: '#f0f0f0', borderBottom: '1px solid #ccc' }}>
          <ul style={{ listStyle: 'none', padding: 0, margin: 0, display: 'flex', justifyContent: 'center', gap: '20px' }}>
            <li>
              <Link to="/">Home</Link>
            </li>
            {isLoggedIn ? (
              <>
                <li>
                  <Link to="/dashboard">Dashboard</Link>
                </li>
                <li>
                  <Link to="/events">Events</Link> {/* This now links to your EventsPage with the list */}
                </li>
                {/* --- CONDITIONAL RENDERING BASED ON ROLE --- */}
                {hasRole('ORGANIZER') && ( // Only show if user is an ORGANIZER
                  <li>
                    <Link to="/create-event">Create Event</Link> {/* <-- Link to CreateEventPage */}
                  </li>
                )}
                {/* --- END CONDITIONAL RENDERING --- */}
                <li>
                  <Link to="/profile">Profile</Link>
                </li>
                <li>
                  <button onClick={handleLogout} style={{ background: 'none', border: 'none', color: '#007bff', cursor: 'pointer', fontSize: '1em' }}>
                    Logout ({currentUser} {currentUserRole ? `(${currentUserRole})` : ''})
                  </button>
                </li>
              </>
            ) : (
              <li>
                <Link to="/login">Login</Link>
              </li>
            )}
          </ul>
        </nav>

        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/login" element={<LoginPage />} />
          <Route
            path="/dashboard"
            element={
              <PrivateRoute>
                <DashboardPage />
              </PrivateRoute>
            }
          />
          <Route
            path="/events"
            element={
              <PrivateRoute>
                <EventsPage /> {/* This route will now render the list of events */}
              </PrivateRoute>
            }
          />
          <Route
            path="/profile"
            element={
              <PrivateRoute>
                <UserProfilePage />
              </PrivateRoute>
            }
          />
          {/* --- PROTECTED ROUTE FOR CREATE EVENT --- */}
          <Route
            path="/create-event"
            element={
              <PrivateRoute>
                <CreateEventPage /> {/* This route is correct for CreateEventPage */}
              </PrivateRoute>
            }
          />
          {/* --- END PROTECTED ROUTE --- */}
        </Routes>
      </div>
    </Router>
  );
}

export default App;