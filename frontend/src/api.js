const BASE_URL = 'http://localhost:8080/api';

const handleResponse = async (response) => {
  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    throw new Error(errorData.error || response.statusText || 'API Error');
  }
  return response.json();
};

export const fetchProducts = () => {
  return fetch(`${BASE_URL}/products`).then(handleResponse);
};

export const fetchPendingPricingSuggestions = () => {
  return fetch(`${BASE_URL}/pricing-suggestions?status=PENDING`).then(handleResponse);
};

export const fetchPendingReorderSuggestions = () => {
  return fetch(`${BASE_URL}/reorder-suggestions?status=PENDING`).then(handleResponse);
};

export const simulateSale = (productId) => {
  return fetch(`${BASE_URL}/products/${productId}/orders`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
  }).then(handleResponse);
};

export const updatePricingSuggestion = (suggestionId, status) => {
  return fetch(`${BASE_URL}/pricing-suggestions/${suggestionId}`, {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ status }),
  }).then(handleResponse);
};

export const updateReorderSuggestion = (suggestionId, status) => {
  return fetch(`${BASE_URL}/reorder-suggestions/${suggestionId}`, {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ status }),
  }).then(handleResponse);
};
