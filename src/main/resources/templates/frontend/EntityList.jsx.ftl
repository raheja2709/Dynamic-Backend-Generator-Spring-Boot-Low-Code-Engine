import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import apiClient from '../api/apiClient';

function ${entity.name}List() {
  const [items, setItems] = useState([]);
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(true);
  const pageSize = 10;

  useEffect(() => {
    fetchData();
  }, [page]);

  async function fetchData() {
    setLoading(true);
    try {
      const res = await apiClient.get('/${entity.name?lower_case}s');
      setItems(Array.isArray(res.data) ? res.data : []);
    } catch (err) {
      console.error('Failed to fetch ${entity.name?lower_case}s', err);
    } finally {
      setLoading(false);
    }
  }

  async function handleDelete(id) {
    if (!window.confirm('Are you sure you want to delete this ${entity.name?lower_case}?')) return;
    try {
      await apiClient.delete('/${entity.name?lower_case}s/' + id);
      fetchData();
    } catch (err) {
      console.error('Delete failed', err);
    }
  }

  if (loading) return <p>Loading...</p>;

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <h1>${entity.name}s</h1>
        <Link to="/${entity.name?lower_case}s/new">
          <button style={{ padding: '0.5rem 1rem', background: '#4fc3f7', border: 'none', borderRadius: 4, color: '#fff', cursor: 'pointer' }}>
            + New ${entity.name}
          </button>
        </Link>
      </div>

      <table style={{ width: '100%', borderCollapse: 'collapse', background: '#fff', borderRadius: 8, overflow: 'hidden', boxShadow: '0 2px 4px rgba(0,0,0,0.1)' }}>
        <thead>
          <tr style={{ background: '#f0f0f0' }}>
<#list fields as field>
            <th style={{ padding: '0.75rem', textAlign: 'left' }}>${field.name?cap_first}</th>
</#list>
            <th style={{ padding: '0.75rem', textAlign: 'left' }}>Actions</th>
          </tr>
        </thead>
        <tbody>
          {items.slice(page * pageSize, (page + 1) * pageSize).map(item => (
            <tr key={item.id} style={{ borderTop: '1px solid #eee' }}>
<#list fields as field>
              <td style={{ padding: '0.75rem' }}>{String(item.${field.name?uncap_first} ?? '')}</td>
</#list>
              <td style={{ padding: '0.75rem' }}>
                <Link to={'/${entity.name?lower_case}s/' + item.id}>View</Link>
                {' | '}
                <Link to={'/${entity.name?lower_case}s/' + item.id + '/edit'}>Edit</Link>
                {' | '}
                <button onClick={() => handleDelete(item.id)} style={{ color: 'red', background: 'none', border: 'none', cursor: 'pointer' }}>
                  Delete
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      <div style={{ marginTop: '1rem', display: 'flex', gap: '0.5rem' }}>
        <button disabled={page === 0} onClick={() => setPage(p => p - 1)}>Previous</button>
        <span>Page {page + 1}</span>
        <button disabled={items.length <= (page + 1) * pageSize} onClick={() => setPage(p => p + 1)}>Next</button>
      </div>
    </div>
  );
}

export default ${entity.name}List;
