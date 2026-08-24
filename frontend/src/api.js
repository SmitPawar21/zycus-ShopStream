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

export const fetchAvailableStrategies = () => {
  return fetch(`${BASE_URL}/strategies/available`).then(handleResponse);
};

export const fetchActiveStrategies = () => {
  return fetch(`${BASE_URL}/strategies/active`).then(handleResponse);
};

export const activatePricingStrategy = (name) => {
  return fetch(`${BASE_URL}/strategies/activate/pricing/${name}`, { method: 'POST' }).then(handleResponse);
};

export const activateReorderStrategy = (name) => {
  return fetch(`${BASE_URL}/strategies/activate/reorder/${name}`, { method: 'POST' }).then(handleResponse);
};

export const generatePricingSuggestion = (productId) => {
  return fetch(`${BASE_URL}/products/${productId}/suggest-pricing/strategy`, { method: 'POST' }).then(handleResponse);
};

export const generateReorderSuggestion = (productId) => {
  return fetch(`${BASE_URL}/products/${productId}/suggest-reorder/strategy`, { method: 'POST' }).then(handleResponse);
};

export const updateStock = (productId, stockLevel) => {
  return fetch(`${BASE_URL}/products/${productId}/stock`, {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ stockLevel: parseInt(stockLevel, 10) }),
  }).then(handleResponse);
};

export const createProduct = (productDto) => {
  return fetch(`${BASE_URL}/products`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(productDto),
  }).then(handleResponse);
};
