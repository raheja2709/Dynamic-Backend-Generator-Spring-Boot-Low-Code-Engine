import React from 'react';
import { Link, useLocation } from 'react-router-dom';

const navItems = [
  { path: '/', label: 'Dashboard', icon: '📊' },
<#list entities as entity>
  { path: '/${entity.name?lower_case}s', label: '${entity.name}s', icon: '📋' },
</#list>
];

function Layout({ children }) {
  const location = useLocation();

  return (
    <div style={{ display: 'flex', minHeight: '100vh' }}>
      <aside style={{ width: 240, background: '#1a1a2e', color: '#fff', padding: '1rem 0' }}>
        <h2 style={{ textAlign: 'center', padding: '0 1rem', marginBottom: '2rem' }}>
          ${project.name}
        </h2>
        <nav>
          {navItems.map(item => (
            <Link
              key={item.path}
              to={item.path}
              style={{
                display: 'block',
                padding: '0.75rem 1.5rem',
                color: location.pathname === item.path ? '#4fc3f7' : '#ccc',
                textDecoration: 'none',
                background: location.pathname === item.path ? 'rgba(79,195,247,0.1)' : 'transparent',
                borderLeft: location.pathname === item.path ? '3px solid #4fc3f7' : '3px solid transparent'
              }}
            >
              {item.icon} {item.label}
            </Link>
          ))}
        </nav>
      </aside>
      <main style={{ flex: 1, padding: '2rem', background: '#f5f5f5' }}>
        {children}
      </main>
    </div>
  );
}

export default Layout;
