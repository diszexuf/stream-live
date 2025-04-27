import axios from 'axios';

const API_URL = '/api/streams';

export const getAllStreams = async () => {
  const response = await axios.get(API_URL);
  return response.data;
};

export const getLiveStreams = async () => {
  const response = await axios.get(`${API_URL}/live`);
  return response.data;
};

export const getStreamById = async (id) => {
  const response = await axios.get(`${API_URL}/${id}`);
  return response.data;
};

export const getStreamsByUser = async (userId) => {
  const response = await axios.get(`${API_URL}/user/${userId}`);
  return response.data;
};

export const getStreamsByCategory = async (categoryId) => {
  const response = await axios.get(`${API_URL}/category/${categoryId}`);
  return response.data;
};

export const createStream = async (stream, userId, categoryId) => {
  const response = await axios.post(API_URL, stream, {
    params: { userId, categoryId }
  });
  return response.data;
};

export const updateStream = async (id, stream) => {
  const response = await axios.put(`${API_URL}/${id}`, stream);
  return response.data;
};

export const startStream = async (id) => {
  const response = await axios.post(`${API_URL}/${id}/start`);
  return response.data;
};

export const endStream = async (id) => {
  const response = await axios.post(`${API_URL}/${id}/end`);
  return response.data;
};

export const deleteStream = async (id) => {
  await axios.delete(`${API_URL}/${id}`);
};

export const resetStreamKey = async (id, userId) => {
  const response = await axios.post(`${API_URL}/${id}/reset-key`, null, {
    params: { userId }
  });
  return response.data;
}; 