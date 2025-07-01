// frontend/src/pages/EventsListPage.tsx
import React, { useEffect, useState } from 'react';
import api from '../api/axiosConfig'; // Your configured Axios instance
import axios from 'axios'; // For axios.isAxiosError
import { useNavigate } from 'react-router-dom'; // Import useNavigate for potential redirection

interface EventResponse {
  id: number;
  title: string;
  description: string;
  eventDate: string; // ISO string for LocalDateTime
  location: string;
  capacity: number;
  active: boolean;
  requiredSkills: string;
  // If your EventResponse includes organizer/organization summary DTOs, you can add them here:
  // organizer?: { id: number; username: string; /* ... other user summary fields */ };
  // organization?: { id: number; name: string; /* ... other organization summary fields */ };
  // registeredVolunteers?: Array<{ id: number; username: string; /* ... */ }>;
}

const EventsListPage: React.FC = () => {
  const [events, setEvents] = useState<EventResponse[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchEvents = async () => {
      try {
        setLoading(true);
        setError(null);
        // The /api/events endpoint requires authentication as per your EventController
        // The 'api' instance already includes the auth token from localStorage
        const response = await api.get<EventResponse[]>('/events');
        setEvents(response.data);
      } catch (err) {
        console.error('Failed to fetch events:', err);
        if (axios.isAxiosError(err) && err.response) {
          setError(err.response.data.message || 'Error fetching events.');
          // Optional: If 401/403, redirect to login
          if (err.response.status === 401 || err.response.status === 403) {
            // navigate('/login'); // Uncomment if you want to redirect to login
          }
        } else {
          setError('An unexpected error occurred while fetching events.');
        }
      } finally {
        setLoading(false);
      }
    };

    fetchEvents();
  }, []); // Empty dependency array means this runs once on component mount

  if (loading) {
    return <div style={styles.container}>Loading events...</div>;
  }

  if (error) {
    return <div style={{ ...styles.container, color: 'red' }}>Error: {error}</div>;
  }

  return (
    <div style={styles.container}>
      <h2 style={styles.header}>All Events</h2>
      {events.length === 0 ? (
        <p style={styles.noEvents}>No events available. Create one!</p>
      ) : (
        <div style={styles.eventList}>
          {events.map((event) => (
            <div key={event.id} style={styles.eventCard}>
              <h3 style={styles.eventTitle}>{event.title}</h3>
              <p style={styles.eventDetail}><strong>Description:</strong> {event.description}</p>
              <p style={styles.eventDetail}><strong>Date:</strong> {new Date(event.eventDate).toLocaleString()}</p>
              <p style={styles.eventDetail}><strong>Location:</strong> {event.location}</p>
              <p style={styles.eventDetail}><strong>Capacity:</strong> {event.capacity}</p>
              {event.requiredSkills && (
                <p style={styles.eventDetail}><strong>Skills:</strong> {event.requiredSkills}</p>
              )}
              {/* Add more event details here if available in EventResponse and you want to display them */}
              {/* <p>Active: {event.active ? 'Yes' : 'No'}</p> */}
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

const styles = {
  container: {
    maxWidth: '800px',
    margin: '50px auto',
    padding: '25px',
    border: '1px solid #ddd',
    borderRadius: '10px',
    boxShadow: '0 4px 12px rgba(0,0,0,0.08)',
    backgroundColor: '#fff',
    textAlign: 'center' as 'center',
  },
  header: {
    textAlign: 'center' as 'center',
    color: '#333',
    marginBottom: '30px',
  },
  noEvents: {
    color: '#666',
    fontSize: '1.1em',
  },
  eventList: {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))',
    gap: '20px',
    marginTop: '20px',
  },
  eventCard: {
    border: '1px solid #eee',
    borderRadius: '8px',
    padding: '20px',
    textAlign: 'left' as 'left',
    boxShadow: '0 2px 8px rgba(0,0,0,0.05)',
    backgroundColor: '#f9f9f9',
    transition: 'transform 0.2s ease-in-out',
  },
  eventCardHover: { // Example for hover effect if you apply it
    transform: 'translateY(-5px)',
  },
  eventTitle: {
    color: '#0056b3',
    marginBottom: '10px',
  },
  eventDetail: {
    fontSize: '0.95em',
    color: '#555',
    marginBottom: '5px',
  },
};

export default EventsListPage;