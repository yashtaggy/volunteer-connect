// frontend/src/pages/EventsPage.tsx
import React, { useEffect, useState } from 'react';
import api from '../api/axiosConfig'; // Import your configured axios instance
import { useNavigate } from 'react-router-dom';

interface Event {
  id: number;
  title: string;
  description: string;
  // Add other event properties as per your backend Event model
}

const EventsPage: React.FC = () => {
  const [events, setEvents] = useState<Event[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchEvents = async () => {
      try {
        setLoading(true);
        setError(null);
        // Use the 'api' instance which automatically attaches the JWT
        const response = await api.get('/events'); // Assuming your events endpoint is /api/events
        setEvents(response.data);
      } catch (err) {
        console.error('Failed to fetch events:', err);
        if (axios.isAxiosError(err) && err.response) {
          if (err.response.status === 401 || err.response.status === 403) {
            setError('Unauthorized: Please log in to view events.');
            // Optionally, clear token and redirect to login if session expired
            localStorage.removeItem('jwtToken');
            localStorage.removeItem('userRoles');
            localStorage.removeItem('userId');
            localStorage.removeItem('username');
            window.dispatchEvent(new Event('storage')); // Notify App.tsx
            navigate('/login');
          } else {
            setError(err.response.data.message || 'Error fetching events.');
          }
        } else {
          setError('An unexpected error occurred while fetching events.');
        }
      } finally {
        setLoading(false);
      }
    };

    fetchEvents();
  }, [navigate]);

  if (loading) return <div>Loading events...</div>;
  if (error) return <div style={{ color: 'red' }}>Error: {error}</div>;
  if (events.length === 0) return <div>No events found.</div>;

  return (
    <div style={{ padding: '20px' }}>
      <h2>Available Events</h2>
      <ul style={{ listStyle: 'none', padding: 0 }}>
        {events.map((event) => (
          <li key={event.id} style={{ marginBottom: '15px', border: '1px solid #eee', padding: '10px', borderRadius: '5px' }}>
            <h3>{event.title}</h3>
            <p>{event.description}</p>
            {/* Render other event details */}
          </li>
        ))}
      </ul>
    </div>
  );
};

export default EventsPage;