import React from 'react';
import { Routes, Route } from 'react-router-dom';
import Layout from './components/Layout';
import Dashboard from './pages/Dashboard';
<#list entities as entity>
import ${entity.name}List from './pages/${entity.name}List';
import ${entity.name}Form from './pages/${entity.name}Form';
import ${entity.name}Detail from './pages/${entity.name}Detail';
</#list>

function App() {
  return (
    <Layout>
      <Routes>
        <Route path="/" element={<Dashboard />} />
<#list entities as entity>
        <Route path="/${entity.name?lower_case}s" element={<${entity.name}List />} />
        <Route path="/${entity.name?lower_case}s/new" element={<${entity.name}Form />} />
        <Route path="/${entity.name?lower_case}s/:id" element={<${entity.name}Detail />} />
        <Route path="/${entity.name?lower_case}s/:id/edit" element={<${entity.name}Form />} />
</#list>
      </Routes>
    </Layout>
  );
}

export default App;
