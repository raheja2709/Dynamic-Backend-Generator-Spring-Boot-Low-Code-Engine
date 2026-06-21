import React, { useState, useEffect } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import apiClient from '../api/apiClient';

function ${entity.name}Detail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [item, setItem] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function fetchItem() {
      try {
        const res = await apiClient.get('/${entity.name?lower_case}s/' + id);
        setItem(res.data);
      } catch (err) {
        console.error('Failed to fetch ${entity.name?lower_case}', err);
      } finally {
        setLoading(false);
      }
    }
    fetchItem();
  }, [id]);

  async function handleDelete() {
    if (!window.confirm('Are you sure you want to delete this ${entity.name?lower_case}?')) return;
    try {
      await apiClient.delete('/${entity.name?lower_case}s/' + id);
      navigate('/${entity.name?lower_case}s');
    } catch (err) {
      console.error('Delete failed', err);
    }
  }

  if (loading) return <p>Loading...</p>;
  if (!item) return <p>${entity.name} not found.</p>;

  return (
    <div style={{ maxWidth: 600 }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <h1>${entity.name} Detail</h1>
        <div style={{ display: 'flex', gap: '0.5rem' }}>
          <Link to={'/${entity.name?lower_case}s/' + id + '/edit'}>
            <button style={{ padding: '0.5rem 1rem', background: '#4fc3f7', border: 'none', borderRadius: 4, color: '#fff', cursor: 'pointer' }}>Edit</button>
          </Link>
          <button onClick={handleDelete} style={{ padding: '0.5rem 1rem', background: '#ef5350', border: 'none', borderRadius: 4, color: '#fff', cursor: 'pointer' }}>Delete</button>
        </div>
      </div>

      <div style={{ background: '#fff', padding: '1.5rem', borderRadius: 8, boxShadow: '0 2px 4px rgba(0,0,0,0.1)', marginTop: '1rem' }}>
<#list fields as field>
        <div style={{ marginBottom: '1rem' }}>
          <strong>${field.name?cap_first}:</strong>
          <span style={{ marginLeft: '0.5rem' }}>{String(item.${field.name?uncap_first} ?? '-')}</span>
        </div>
</#list>
      </div>

      <Link to="/${entity.name?lower_case}s" style={{ display: 'inline-block', marginTop: '1rem' }}>← Back to list</Link>
    </div>
  );
}

export default ${entity.name}Detail;
