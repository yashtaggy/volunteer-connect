// frontend/src/pages/CreateEventPage.tsx
import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/axiosConfig'; // Your configured Axios instance
import axios from 'axios'; // For axios.isAxiosError

const CreateEventPage: React.FC = () => {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    title: '',
    description: '',
    location: '',
    date: '', // Will be "yyyy-MM-dd" format
    time: '', // Will be "HH:mm" format
    requiredSkills: '', // Comma-separated string
    capacity: 0,
  });
  const [message, setMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState<boolean>(true); // For initial role check/loading
  const [isSubmitting, setIsSubmitting] = useState<boolean>(false); // For form submission state

  useEffect(() => {
    // Client-side role check before showing the form
    const userRolesString = localStorage.getItem('userRoles');
    let userRole: string | null = null;
    if (userRolesString) {
      try {
        const rolesArray = JSON.parse(userRolesString);
        userRole = rolesArray[0]; // Get the primary role (assuming first role is sufficient)
      } catch (e) {
        console.error("Failed to parse user roles from localStorage", e);
      }
    }

    if (userRole !== 'ORGANIZER') {
      setError("You do not have permission to create events. Only ORGANIZERS can.");
      setLoading(false);
      return;
    }

    setLoading(false); // If Organizer, stop loading and show form
  }, [navigate]);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setFormData((prevData) => ({
      ...prevData,
      [name]: name === 'capacity' ? parseInt(value) || 0 : value, // Convert capacity to number
    }));
    setError(null); // Clear errors on input change
    setMessage(null); // Clear success message on input change
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsSubmitting(true); // Indicate submission in progress
    setError(null);
    setMessage(null);

    // Basic validation before sending
    if (!formData.title || !formData.description || !formData.location || !formData.date || !formData.time || formData.capacity <= 0) {
      setError('Please fill in all required fields and ensure capacity is greater than 0.');
      setIsSubmitting(false);
      return;
    }

    // Format the date and time for LocalDateTime on the backend
    // Backend expects ISO 8601 format (e.g., "YYYY-MM-DDTHH:mm:ss")
    // Ensure you add seconds (':00') if your backend's LocalDateTime is strict
    const eventDateTime = `${formData.date}T${formData.time}:00`; 

    try {
      const eventData = {
        title: formData.title,
        description: formData.description,
        eventDate: eventDateTime, // Use the formatted date and time
        location: formData.location,
        capacity: formData.capacity, // Already parsed to number in handleChange
        requiredSkills: formData.requiredSkills,
        // --- IMPORTANT: HARDCODED ORGANIZATION ID FOR TESTING ---
        // REPLACE '1' WITH THE ACTUAL ID OF THE ORGANIZATION YOU CREATED IN YOUR DATABASE
        organizationId: 1, // <--- CHANGE THIS TO YOUR ACTUAL ORGANIZATION'S ID (e.g., 1 or 2)
        // -----------------------------------------------------
        active: true, // New events are active by default
        // organizerId is handled by the backend's security context (getCurrentAuthenticatedUserId)
      };

      const response = await api.post('/events', eventData); // Using the 'api' instance
      
      setMessage('Event created successfully!');
      // Reset form after successful submission
      setFormData({
        title: '',
        description: '',
        location: '',
        date: '',
        time: '',
        requiredSkills: '',
        capacity: 0,
      });
      console.log('Event created:', response.data);
      // Optional: Redirect to events list or event details page after creation
      // navigate('/events'); // Uncomment if you want to redirect
    } catch (err) {
      console.error('Failed to create event:', err);
      if (axios.isAxiosError(err) && err.response) {
        // Handle specific backend validation errors or unauthorized access
        // err.response.data might be a string or an object with a 'message' field
        setError(err.response.data.message || JSON.stringify(err.response.data) || 'Error creating event. Check your inputs.');
      } else {
        setError('An unexpected error occurred. Please try again.');
      }
    } finally {
      setIsSubmitting(false); // End submission state
    }
  };

  if (loading) {
    return <div style={{ textAlign: 'center', padding: '20px' }}>Loading permissions...</div>;
  }

  if (error && error.includes("You do not have permission")) {
    return <div style={{ color: 'red', textAlign: 'center', padding: '20px' }}>{error}</div>;
  }

  return (
    <div style={{ maxWidth: '600px', margin: '50px auto', padding: '25px', border: '1px solid #ddd', borderRadius: '10px', boxShadow: '0 4px 12px rgba(0,0,0,0.08)', backgroundColor: '#fff' }}>
      <h2 style={{ textAlign: 'center', color: '#333', marginBottom: '30px' }}>Create New Event</h2>
      {message && <p style={{ color: 'green', textAlign: 'center', fontWeight: 'bold' }}>{message}</p>}
      {error && <p style={{ color: 'red', textAlign: 'center', fontWeight: 'bold' }}>{error}</p>}
      <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '15px' }}>
        <div>
          <label htmlFor="title" style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Title:</label>
          <input
            type="text"
            id="title"
            name="title"
            value={formData.title}
            onChange={handleChange}
            required
            style={{ width: '100%', padding: '10px', border: '1px solid #ddd', borderRadius: '4px', boxSizing: 'border-box' }}
          />
        </div>
        <div>
          <label htmlFor="description" style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Description:</label>
          <textarea
            id="description"
            name="description"
            value={formData.description}
            onChange={handleChange}
            required
            rows={4}
            style={{ width: '100%', padding: '10px', border: '1px solid #ddd', borderRadius: '4px', boxSizing: 'border-box', resize: 'vertical' }}
          />
        </div>
        <div>
          <label htmlFor="location" style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Location:</label>
          <input
            type="text"
            id="location"
            name="location"
            value={formData.location}
            onChange={handleChange}
            required
            style={{ width: '100%', padding: '10px', border: '1px solid #ddd', borderRadius: '4px', boxSizing: 'border-box' }}
          />
        </div>
        <div style={{ display: 'flex', gap: '15px' }}>
          <div style={{ flex: 1 }}>
            <label htmlFor="date" style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Date:</label>
            <input
              type="date"
              id="date"
              name="date"
              value={formData.date}
              onChange={handleChange}
              required
              style={{ width: '100%', padding: '10px', border: '1px solid #ddd', borderRadius: '4px', boxSizing: 'border-box' }}
            />
          </div>
          <div style={{ flex: 1 }}>
            <label htmlFor="time" style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Time:</label>
            <input
              type="time"
              id="time"
              name="time"
              value={formData.time}
              onChange={handleChange}
              required
              style={{ width: '100%', padding: '10px', border: '1px solid #ddd', borderRadius: '4px', boxSizing: 'border-box' }}
            />
          </div>
        </div>
        <div>
          <label htmlFor="requiredSkills" style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Required Skills (comma-separated):</label>
          <input
            type="text"
            id="requiredSkills"
            name="requiredSkills"
            value={formData.requiredSkills}
            onChange={handleChange}
            placeholder="e.g., first aid, communication, lifting"
            style={{ width: '100%', padding: '10px', border: '1px solid #ddd', borderRadius: '4px', boxSizing: 'border-box' }}
          />
        </div>
        <div>
          <label htmlFor="capacity" style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Capacity:</label>
          <input
            type="number"
            id="capacity"
            name="capacity"
            value={formData.capacity}
            onChange={handleChange}
            required
            min="1"
            style={{ width: '100%', padding: '10px', border: '1px solid #ddd', borderRadius: '4px', boxSizing: 'border-box' }}
          />
        </div>
        <button
          type="submit"
          disabled={isSubmitting} // Use isSubmitting to disable button
          style={{ padding: '12px 20px', backgroundColor: '#007bff', color: 'white', border: 'none', borderRadius: '5px', cursor: 'pointer', fontSize: '1.1em', marginTop: '20px', opacity: isSubmitting ? 0.7 : 1 }}
        >
          {isSubmitting ? 'Creating Event...' : 'Create Event'}
        </button>
      </form>
    </div>
  );
};

export default CreateEventPage;