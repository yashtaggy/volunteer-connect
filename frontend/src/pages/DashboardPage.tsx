import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';

const DashboardPage: React.FC = () => {
  const navigate = useNavigate();
  const [username, setUsername] = useState<string | null>(null);
  const [userRole, setUserRole] = useState<string | null>(null);

  useEffect(() => {
    const token = localStorage.getItem('jwtToken');
    const storedUsername = localStorage.getItem('username'); // We need to store username during login
    const storedRoles = localStorage.getItem('userRoles'); // This is already stored as JSON array string

    if (!token) {
      // If no token, user is not authenticated, redirect to login
      navigate('/login');
    } else {
      // User is authenticated, retrieve user info
      setUsername(storedUsername);
      if (storedRoles) {
        try {
          const rolesArray = JSON.parse(storedRoles);
          // Assuming you want to display the first role or a comma-separated list
          setUserRole(rolesArray[0] || 'Unknown Role');
        } catch (e) {
          console.error("Failed to parse user roles from localStorage", e);
          setUserRole('Invalid Role Data');
        }
      }
    }
  }, [navigate]); // navigate as dependency to useEffect

  if (!username) {
    // Optionally show a loading state or nothing while redirecting
    return <div>Loading dashboard...</div>;
  }

  return (
    <div style={{ padding: '20px', textAlign: 'center' }}>
      <h2>Welcome, {username}!</h2>
      <p>You are logged in as a: **{userRole}**</p>
      <p>This is your personalized dashboard. More content will go here.</p>
      {/* Add dashboard specific content here later */}
    </div>
  );
};

export default DashboardPage;