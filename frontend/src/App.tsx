import React from 'react';
    import './App.css'; // Make sure this path is correct

    // Import routing components
    import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom';

    // Import your pages
    import Home from './pages/Home';
    import LoginPage from './pages/LoginPage';

    function App() {
      return (
        <Router> {/* BrowserRouter wraps your entire application */}
          <div className="App">
            <nav>
              <ul>
                <li>
                  <Link to="/">Home</Link> {/* Link to the Home page */}
                </li>
                <li>
                  <Link to="/login">Login</Link> {/* Link to the Login page */}
                </li>
                {/* Add more navigation links here as you create more pages */}
              </ul>
            </nav>

            {/* Define your routes */}
            <Routes> {/* Routes component groups all your individual Route components */}
              <Route path="/" element={<Home />} /> {/* Renders Home component for '/' path */}
              <Route path="/login" element={<LoginPage />} /> {/* Renders LoginPage component for '/login' path */}
              {/* Add more routes here for other pages */}
            </Routes>
          </div>
        </Router>
      );
    }

    export default App;