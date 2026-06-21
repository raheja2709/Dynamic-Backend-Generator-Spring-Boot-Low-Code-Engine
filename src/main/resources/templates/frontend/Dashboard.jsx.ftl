import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import apiClient from '../api/apiClient';

function Dashboard() {
  const [counts, setCounts] = useState({});
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function fetchCounts() {
      try {
        const results = {};
<#list entities as entity>
        const ${entity.name?uncap_first}Res = await apiClient.get('/${entity.name?lower_case}s');
        results['${entity.name}'] = Array.isArray(${entity.name?uncap_first}Res.data) ? ${entity.name?uncap_first}Res.data.length : 0;
</#list>
        setCounts(results);
      } catch (err) {
        console.error('Failed to fetch counts', err);
      } finally {
        setLoading(false);
      }
    }
    fetchCounts();
  }, []);

  if (loading) return <p>Loading...</p>;

  return (
    <div>
      <h1>Dashboard</h1>
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(200px, 1fr))', gap: '1rem', marginTop: '1rem' }}>
<#list entities as entity>
        <Link to="/${entity.name?lower_case}s" style={{ textDecoration: 'none' }}>
          <div style={{ background: '#fff', padding: '1.5rem', borderRadius: 8, boxShadow: '0 2px 4px rgba(0,0,0,0.1)' }}>
            <h3 style={{ margin: 0, color: '#333' }}>${entity.name}s</h3>
            <p style={{ fontSize: '2rem', margin: '0.5rem 0 0', color: '#4fc3f7' }}>{counts['${entity.name}'] || 0}</p>
          </div>
        </Link>
</#list>
      </div>
    </div>
  );
}

export default Dashboard;
