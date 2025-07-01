// frontend/src/pages/EventsPage.tsx
import React, { useEffect, useState } from 'react';
import api from '../api/axiosConfig'; // Your configured Axios instance
import axios from 'axios'; // For axios.isAxiosError
import { useNavigate } from 'react-router-dom';
import moment from 'moment'; // For date comparisons

// You might already have these imports in App.tsx or a global type file.
// If not, ensure these DTOs match your backend's EventResponse structure.
interface UserSummaryDto {
  id: number;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
}

interface OrganizationSummaryDto {
  id: number;
  name: string;
  contactEmail: string;
}

interface EventResponse {
  id: number;
  title: string;
  description: string;
  eventDate: string; // ISO string for LocalDateTime
  location: string;
  capacity: number;
  active: boolean;
  requiredSkills: string;
  organizer?: UserSummaryDto;
  organization?: OrganizationSummaryDto;
  registeredVolunteers?: UserSummaryDto[]; // List of users registered for the event
}

const EventsPage: React.FC = () => {
  const [events, setEvents] = useState<EventResponse[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  const navigate = useNavigate();

  // Get current user details from localStorage
  const currentUserId = localStorage.getItem('userId');
  const currentUserRoleString = localStorage.getItem('userRoles');
  const currentUserRole = currentUserRoleString ? JSON.parse(currentUserRoleString)[0] : null; // Assuming first role is primary

  const fetchEvents = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await api.get<EventResponse[]>('/events');
      setEvents(response.data);
    } catch (err) {
      console.error('Failed to fetch events:', err);
      if (axios.isAxiosError(err) && err.response) {
        setError(err.response.data.message || 'Error fetching events.');
      } else {
        setError('An unexpected error occurred while fetching events.');
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchEvents();
  }, []);

  const handleRegisterClick = async (eventId: number) => {
    if (!currentUserId || currentUserRole !== 'VOLUNTEER') {
      alert('You must be logged in as a VOLUNTEER to register for events.');
      navigate('/login'); // Redirect to login if not a volunteer
      return;
    }

    try {
      setSuccessMessage(null); // Clear previous messages
      setError(null);

      // Call the new backend endpoint for registration
      const response = await api.post(`/events/${eventId}/register`);

      // If successful, update the specific event in the state to reflect the new registration count/status
      // The backend returns the updated event, so we can use that to refresh.
      setEvents(prevEvents =>
        prevEvents.map(event =>
          event.id === eventId ? response.data : event // Replace the old event with the updated one
        )
      );
      setSuccessMessage('Successfully registered for the event!');
      setTimeout(() => setSuccessMessage(null), 5000); // Clear message after 5 seconds

    } catch (err) {
      console.error('Registration failed:', err);
      if (axios.isAxiosError(err) && err.response) {
        setError(err.response.data.message || 'Failed to register for the event.');
      } else {
        setError('An unexpected error occurred during registration.');
      }
      setTimeout(() => setError(null), 7000); // Clear message after 7 seconds
    }
  };

  const isVolunteer = currentUserRole === 'VOLUNTEER';

  if (loading) {
    return <div style={styles.container}>Loading events...</div>;
  }

  return (
    <div style={styles.container}>
      <h2 style={styles.header}>All Events</h2>
      {successMessage && <div style={styles.successMessage}>{successMessage}</div>}
      {error && <div style={styles.errorMessage}>Error: {error}</div>}

      {events.length === 0 ? (
        <p style={styles.noEvents}>No events available. Check back later or consider creating one if you are an organizer!</p>
      ) : (
        <div style={styles.eventList}>
          {events.map((event) => {
            const eventMoment = moment(event.eventDate);
            const isEventInPast = eventMoment.isBefore(moment());
            const isEventFull = (event.registeredVolunteers?.length || 0) >= event.capacity;
            const isRegistered = event.registeredVolunteers?.some(
              (volunteer) => volunteer.id === Number(currentUserId)
            );
            const isOrganizerOfEvent = event.organizer?.id === Number(currentUserId);

            return (
              <div key={event.id} style={styles.eventCard}>
                <h3 style={styles.eventTitle}>{event.title}</h3>
                <p style={styles.eventDetail}>
                  <strong>Description:</strong> {event.description}
                </p>
                <p style={styles.eventDetail}>
                  <strong>Date:</strong> {eventMoment.format('MMMM Do YYYY, h:mm A')}
                </p>
                <p style={styles.eventDetail}>
                  <strong>Location:</strong> {event.location}
                </p>
                <p style={styles.eventDetail}>
                  <strong>Capacity:</strong> {event.registeredVolunteers?.length || 0} / {event.capacity}
                  {isEventFull && <span style={styles.fullCapacity}> (Full)</span>}
                </p>
                {event.requiredSkills && (
                  <p style={styles.eventDetail}>
                    <strong>Skills:</strong> {event.requiredSkills}
                  </p>
                )}
                {event.organizer && (
                  <p style={styles.eventDetail}>
                    <strong>Organizer:</strong> {event.organizer.firstName} {event.organizer.lastName} ({event.organizer.username})
                  </p>
                )}
                {event.organization && (
                  <p style={styles.eventDetail}>
                    <strong>Organization:</strong> {event.organization.name}
                  </p>
                )}

                <div style={styles.buttonContainer}>
                  {isVolunteer && !isOrganizerOfEvent && ( // Only show for volunteers who are not the organizer
                    <button
                      onClick={() => handleRegisterClick(event.id)}
                      disabled={isEventInPast || isEventFull || isRegistered || !event.active}
                      style={{
                        ...styles.registerButton,
                        ...(isEventInPast || isEventFull || isRegistered || !event.active ? styles.disabledButton : {}),
                      }}
                    >
                      {isEventInPast
                        ? 'Event in Past'
                        : !event.active
                        ? 'Event Inactive'
                        : isRegistered
                        ? 'Registered'
                        : isEventFull
                        ? 'Full'
                        : 'Register'}
                    </button>
                  )}
                  {/* Optional: Add a "View Details" button here, which could navigate to /events/:id */}
                  {/* <button style={styles.viewDetailsButton}>View Details</button> */}
                </div>
              </div>
            );
          })}
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
    display: 'flex', // Use flexbox for button positioning
    flexDirection: 'column' as 'column',
    justifyContent: 'space-between', // Push button to bottom
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
  fullCapacity: {
    color: 'red',
    fontWeight: 'bold',
    marginLeft: '5px',
  },
  buttonContainer: {
    marginTop: '15px',
    display: 'flex',
    gap: '10px',
    justifyContent: 'flex-end', // Align buttons to the right
  },
  registerButton: {
    padding: '10px 15px',
    backgroundColor: '#28a745',
    color: 'white',
    border: 'none',
    borderRadius: '5px',
    cursor: 'pointer',
    fontSize: '0.9em',
    transition: 'background-color 0.2s ease-in-out',
    flexShrink: 0, // Prevent button from shrinking
  },
  disabledButton: {
    backgroundColor: '#6c757d',
    cursor: 'not-allowed',
  },
  viewDetailsButton: {
    padding: '10px 15px',
    backgroundColor: '#007bff',
    color: 'white',
    border: 'none',
    borderRadius: '5px',
    cursor: 'pointer',
    fontSize: '0.9em',
    transition: 'background-color 0.2s ease-in-out',
  },
  successMessage: {
    backgroundColor: '#d4edda',
    color: '#155724',
    border: '1px solid #c3e6cb',
    padding: '10px',
    marginBottom: '20px',
    borderRadius: '5px',
  },
  errorMessage: {
    backgroundColor: '#f8d7da',
    color: '#721c24',
    border: '1px solid #f5c6cb',
    padding: '10px',
    marginBottom: '20px',
    borderRadius: '5px',
  },
};

export default EventsPage;