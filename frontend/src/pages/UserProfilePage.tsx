// frontend/src/pages/UserProfilePage.tsx
import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/axiosConfig'; // Your configured Axios instance
import axios from 'axios'; // For axios.isAxiosError

interface UserProfile {
  id: number;
  username: string;
  email: string;
  firstName: string | null;
  lastName: string | null;
  role: string;
  // Add other fields you want to display/edit
}

const UserProfilePage: React.FC = () => {
  const navigate = useNavigate();
  const [userProfile, setUserProfile] = useState<UserProfile | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [editMode, setEditMode] = useState<boolean>(false);
  const [formData, setFormData] = useState<UserProfile | null>(null);

  const userId = localStorage.getItem('userId'); // Get the logged-in user's ID

  useEffect(() => {
    if (!userId) {
      setError('User ID not found in local storage. Please log in.');
      setLoading(false);
      navigate('/login');
      return;
    }

    const fetchUserProfile = async () => {
      try {
        setLoading(true);
        setError(null);
        // Make a GET request to your backend to fetch user details by ID
        // Replace `/users/${userId}` with your actual backend endpoint
        const response = await api.get(`/users/${userId}`);
        setUserProfile(response.data);
        setFormData(response.data); // Initialize form data with fetched profile
      } catch (err) {
        console.error('Failed to fetch user profile:', err);
        if (axios.isAxiosError(err) && err.response) {
          if (err.response.status === 401 || err.response.status === 403) {
            setError('Authentication required or not authorized to view this profile. Please log in.');
            // Clear token and redirect to login if session expired or unauthorized
            localStorage.removeItem('jwtToken');
            localStorage.removeItem('userRoles');
            localStorage.removeItem('userId');
            localStorage.removeItem('username');
            window.dispatchEvent(new Event('storage'));
            navigate('/login');
          } else {
            setError(err.response.data.message || 'Error fetching user profile.');
          }
        } else {
          setError('An unexpected error occurred while fetching user profile.');
        }
      } finally {
        setLoading(false);
      }
    };

    fetchUserProfile();
  }, [userId, navigate]); // Depend on userId and navigate

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (formData) {
      setFormData({
        ...formData,
        [e.target.name]: e.target.value,
      });
    }
  };

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!formData || !userId) return;

    try {
      setError(null);
      // Make a PUT request to update user details
      // Replace `/users/${userId}` with your actual backend endpoint
      await api.put(`/users/${userId}`, formData);
      setUserProfile(formData); // Update displayed profile with new data
      setEditMode(false); // Exit edit mode
      alert('Profile updated successfully!');
    } catch (err) {
      console.error('Failed to update user profile:', err);
      if (axios.isAxiosError(err) && err.response) {
        setError(err.response.data.message || 'Error updating profile.');
      } else {
        setError('An unexpected error occurred while updating profile.');
      }
    }
  };

  if (loading) {
    return <div style={{ textAlign: 'center', padding: '20px' }}>Loading profile...</div>;
  }

  if (error) {
    return <div style={{ color: 'red', textAlign: 'center', padding: '20px' }}>Error: {error}</div>;
  }

  if (!userProfile) {
    return <div style={{ textAlign: 'center', padding: '20px' }}>No user profile found.</div>;
  }

  return (
    <div style={{ maxWidth: '600px', margin: '50px auto', padding: '25px', border: '1px solid #ddd', borderRadius: '10px', boxShadow: '0 4px 12px rgba(0,0,0,0.08)', backgroundColor: '#fff' }}>
      <h2 style={{ textAlign: 'center', color: '#333', marginBottom: '30px' }}>User Profile</h2>

      {!editMode ? (
        <div style={{ lineHeight: '1.8' }}>
          <p><strong>Username:</strong> {userProfile.username}</p>
          <p><strong>Email:</strong> {userProfile.email}</p>
          <p><strong>First Name:</strong> {userProfile.firstName || 'N/A'}</p>
          <p><strong>Last Name:</strong> {userProfile.lastName || 'N/A'}</p>
          <p><strong>Role:</strong> {userProfile.role}</p>
          <button
            onClick={() => setEditMode(true)}
            style={{ marginTop: '20px', padding: '10px 20px', backgroundColor: '#007bff', color: 'white', border: 'none', borderRadius: '5px', cursor: 'pointer', fontSize: '1em' }}
          >
            Edit Profile
          </button>
        </div>
      ) : (
        <form onSubmit={handleSave} style={{ display: 'flex', flexDirection: 'column', gap: '15px' }}>
          <div>
            <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Username:</label>
            {/* Username usually shouldn't be editable directly here, or requires special handling */}
            <input
              type="text"
              name="username"
              value={formData?.username || ''}
              readOnly // Make username read-only if not intended to be changed
              style={{ width: '100%', padding: '10px', border: '1px solid #ddd', borderRadius: '4px', backgroundColor: '#f0f0f0' }}
            />
          </div>
          <div>
            <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Email:</label>
            <input
              type="email"
              name="email"
              value={formData?.email || ''}
              onChange={handleChange}
              required
              style={{ width: '100%', padding: '10px', border: '1px solid #ddd', borderRadius: '4px' }}
            />
          </div>
          <div>
            <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>First Name:</label>
            <input
              type="text"
              name="firstName"
              value={formData?.firstName || ''}
              onChange={handleChange}
              style={{ width: '100%', padding: '10px', border: '1px solid #ddd', borderRadius: '4px' }}
            />
          </div>
          <div>
            <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Last Name:</label>
            <input
              type="text"
              name="lastName"
              value={formData?.lastName || ''}
              onChange={handleChange}
              style={{ width: '100%', padding: '10px', border: '1px solid #ddd', borderRadius: '4px' }}
            />
          </div>
          {/* Role is usually not editable by the user themselves */}
          <div>
            <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Role:</label>
            <input
              type="text"
              name="role"
              value={formData?.role || ''}
              readOnly
              style={{ width: '100%', padding: '10px', border: '1px solid #ddd', borderRadius: '4px', backgroundColor: '#f0f0f0' }}
            />
          </div>

          {error && <p style={{ color: 'red', marginTop: '10px' }}>{error}</p>}

          <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: '20px' }}>
            <button
              type="submit"
              style={{ padding: '10px 20px', backgroundColor: '#28a745', color: 'white', border: 'none', borderRadius: '5px', cursor: 'pointer', fontSize: '1em', flex: 1, marginRight: '10px' }}
            >
              Save Changes
            </button>
            <button
              type="button"
              onClick={() => { setEditMode(false); setFormData(userProfile); }} // Reset form data to original
              style={{ padding: '10px 20px', backgroundColor: '#6c757d', color: 'white', border: 'none', borderRadius: '5px', cursor: 'pointer', fontSize: '1em', flex: 1 }}
            >
              Cancel
            </button>
          </div>
        </form>
      )}
    </div>
  );
};

export default UserProfilePage;