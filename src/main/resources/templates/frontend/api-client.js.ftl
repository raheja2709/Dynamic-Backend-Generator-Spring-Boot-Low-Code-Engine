import axios from 'axios';

const apiClient = axios.create({
  baseURL: 'http://localhost:8080/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor for auth token
apiClient.interceptors.request.use(config => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = 'Bearer ' + token;
  }
  return config;
});

// Response interceptor for error handling
apiClient.interceptors.response.use(
  response => response,
  error => {
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('token');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default apiClient;

// Entity API methods
<#list entities as entity>
export const ${entity.name?uncap_first}Api = {
  getAll: () => apiClient.get('/${entity.name?lower_case}s'),
  getById: (id) => apiClient.get('/${entity.name?lower_case}s/' + id),
  create: (data) => apiClient.post('/${entity.name?lower_case}s', data),
  update: (id, data) => apiClient.put('/${entity.name?lower_case}s/' + id, data),
  delete: (id) => apiClient.delete('/${entity.name?lower_case}s/' + id),
};
</#list>
