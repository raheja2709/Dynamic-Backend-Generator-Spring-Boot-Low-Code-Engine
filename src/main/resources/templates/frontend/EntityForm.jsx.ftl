import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import apiClient from '../api/apiClient';

function ${entity.name}Form() {
  const { id } = useParams();
  const navigate = useNavigate();
  const isEdit = Boolean(id);

  const [formData, setFormData] = useState({
<#list fields as field>
    ${field.name?uncap_first}: '',
</#list>
  });
<#-- Relationship state for dropdowns/multi-selects -->
<#list relationships as rel>
<#if rel.relationshipType.name() == "MANY_TO_ONE" || rel.relationshipType.name() == "ONE_TO_ONE">
  const [${rel.fieldName}Options, set${rel.fieldName?cap_first}Options] = useState([]);
<#elseif rel.relationshipType.name() == "MANY_TO_MANY">
  const [${rel.fieldName}Options, set${rel.fieldName?cap_first}Options] = useState([]);
</#if>
</#list>
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (isEdit) {
      loadEntity();
    }
    loadRelationships();
  }, [id]);

  async function loadEntity() {
    try {
      const res = await apiClient.get('/${entity.name?lower_case}s/' + id);
      setFormData(res.data);
    } catch (err) {
      setError('Failed to load ${entity.name?lower_case}');
    }
  }

  async function loadRelationships() {
<#list relationships as rel>
    try {
      const res = await apiClient.get('/${rel.targetEntity?lower_case}s');
      set${rel.fieldName?cap_first}Options(Array.isArray(res.data) ? res.data : []);
    } catch (err) {
      console.error('Failed to load ${rel.targetEntity} options', err);
    }
</#list>
  }

  function handleChange(e) {
    const { name, value, type, checked } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value
    }));
  }

<#list relationships as rel>
<#if rel.relationshipType.name() == "MANY_TO_MANY">
  function handle${rel.fieldName?cap_first}Change(e) {
    const selected = Array.from(e.target.selectedOptions, opt => Number(opt.value));
    setFormData(prev => ({ ...prev, ${rel.fieldName}Ids: selected }));
  }
</#if>
</#list>

  async function handleSubmit(e) {
    e.preventDefault();
    setLoading(true);
    setError(null);
    try {
      if (isEdit) {
        await apiClient.put('/${entity.name?lower_case}s/' + id, formData);
      } else {
        await apiClient.post('/${entity.name?lower_case}s', formData);
      }
      navigate('/${entity.name?lower_case}s');
    } catch (err) {
      setError(err.response?.data?.message || 'Save failed');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div style={{ maxWidth: 600 }}>
      <h1>{isEdit ? 'Edit' : 'Create'} ${entity.name}</h1>
      {error && <p style={{ color: 'red' }}>{error}</p>}
      <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
<#list fields as field>
        <div>
          <label htmlFor="${field.name?uncap_first}" style={{ display: 'block', marginBottom: '0.25rem', fontWeight: 500 }}>
            ${field.name?cap_first}<#if !field.nullable> *</#if>
          </label>
          <input
            id="${field.name?uncap_first}"
            name="${field.name?uncap_first}"
            type="${mapFieldToInputType(field.dataType)}"
            value={formData.${field.name?uncap_first} ?? ''}
            onChange={handleChange}
            <#if !field.nullable>required</#if>
            style={{ width: '100%', padding: '0.5rem', border: '1px solid #ddd', borderRadius: 4 }}
          />
        </div>
</#list>
<#-- Relationship fields -->
<#list relationships as rel>
<#if rel.relationshipType.name() == "MANY_TO_ONE" || rel.relationshipType.name() == "ONE_TO_ONE">
        <div>
          <label style={{ display: 'block', marginBottom: '0.25rem', fontWeight: 500 }}>${rel.fieldName?cap_first}</label>
          <select
            name="${rel.fieldName}Id"
            value={formData.${rel.fieldName}Id ?? ''}
            onChange={handleChange}
            style={{ width: '100%', padding: '0.5rem', border: '1px solid #ddd', borderRadius: 4 }}
          >
            <option value="">-- Select ${rel.targetEntity} --</option>
            {${rel.fieldName}Options.map(opt => (
              <option key={opt.id} value={opt.id}>{opt.name || opt.id}</option>
            ))}
          </select>
        </div>
<#elseif rel.relationshipType.name() == "MANY_TO_MANY">
        <div>
          <label style={{ display: 'block', marginBottom: '0.25rem', fontWeight: 500 }}>${rel.fieldName?cap_first}</label>
          <select
            multiple
            value={(formData.${rel.fieldName}Ids || []).map(String)}
            onChange={handle${rel.fieldName?cap_first}Change}
            style={{ width: '100%', padding: '0.5rem', border: '1px solid #ddd', borderRadius: 4, minHeight: 100 }}
          >
            {${rel.fieldName}Options.map(opt => (
              <option key={opt.id} value={opt.id}>{opt.name || opt.id}</option>
            ))}
          </select>
        </div>
</#if>
</#list>

        <button
          type="submit"
          disabled={loading}
          style={{ padding: '0.75rem', background: '#4fc3f7', border: 'none', borderRadius: 4, color: '#fff', cursor: 'pointer', fontSize: '1rem' }}
        >
          {loading ? 'Saving...' : (isEdit ? 'Update' : 'Create')}
        </button>
      </form>
    </div>
  );
}

export default ${entity.name}Form;

<#function mapFieldToInputType dataType>
    <#switch dataType>
        <#case "STRING"><#return "text">
        <#case "TEXT"><#return "text">
        <#case "INTEGER"><#return "number">
        <#case "LONG"><#return "number">
        <#case "DOUBLE"><#return "number">
        <#case "FLOAT"><#return "number">
        <#case "DECIMAL"><#return "number">
        <#case "BOOLEAN"><#return "checkbox">
        <#case "DATE"><#return "date">
        <#case "DATETIME"><#return "datetime-local">
        <#default><#return "text">
    </#switch>
</#function>
