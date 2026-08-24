import React, { useState, useEffect, useCallback } from 'react';
import { 
  fetchProducts, 
  fetchPendingPricingSuggestions, 
  fetchPendingReorderSuggestions, 
  simulateSale, 
  updatePricingSuggestion, 
  updateReorderSuggestion 
} from '../api';
import { ProductRow } from '../components/ProductRow';

export const HomePage = () => {
  const [products, setProducts] = useState([]);
  const [pricingSuggestions, setPricingSuggestions] = useState({});
  const [reorderSuggestions, setReorderSuggestions] = useState({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  
  const loadData = useCallback(async () => {
    try {
      const [productsData, pricingData, reorderData] = await Promise.all([
        fetchProducts(),
        fetchPendingPricingSuggestions(),
        fetchPendingReorderSuggestions()
      ]);
      
      setProducts(productsData);
      
      const pricingMap = {};
      pricingData.forEach(s => pricingMap[s.productId] = s);
      setPricingSuggestions(pricingMap);
      
      const reorderMap = {};
      reorderData.forEach(s => reorderMap[s.productId] = s);
      setReorderSuggestions(reorderMap);
      
      setError(null);
    } catch (err) {
      setError('Failed to fetch data: ' + err.message);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadData();
    const interval = setInterval(loadData, 5000); // Poll every 5s
    return () => clearInterval(interval);
  }, [loadData]);

  const handleSimulateSale = async (productId) => {
    try {
      await simulateSale(productId);
      loadData(); 
    } catch (err) {
      alert('Simulation failed: ' + err.message);
    }
  };
  
  const handlePricingAction = async (id, action) => {
    try {
      await updatePricingSuggestion(id, action);
      loadData();
    } catch (err) {
      alert(`Pricing ${action} failed: ` + err.message);
    }
  };
  
  const handleReorderAction = async (id, action) => {
    try {
      await updateReorderSuggestion(id, action);
      loadData();
    } catch (err) {
      alert(`Reorder ${action} failed: ` + err.message);
    }
  };

  if (loading && products.length === 0) {
    return <div className="p-8 text-sm text-gray-500 font-mono">Loading data...</div>;
  }

  return (
    <div className="min-h-screen bg-gray-100 p-4 sm:p-6 lg:p-8 font-sans">
      <div className="max-w-[1400px] mx-auto">
        <header className="mb-6 flex justify-between items-end border-b border-gray-300 pb-4">
          <div>
            <h1 className="text-2xl font-bold text-gray-900 tracking-tight">Merchandising Operations Console</h1>
            <p className="text-sm text-gray-600 mt-1 font-mono">T-5 Systems // AI Inventory & Pricing Engine</p>
          </div>
          <div className="text-xs text-gray-500 font-mono">
            {products.length} Products Monitored
          </div>
        </header>

        {error && (
          <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-sm text-sm mb-6">
            <strong>System Error:</strong> {error}
          </div>
        )}

        <div className="bg-white border border-gray-300 shadow-sm rounded-sm overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="bg-gray-100 border-b border-gray-300 text-xs uppercase text-gray-700 tracking-wider">
                  <th className="px-4 py-3 font-semibold">Product Details</th>
                  <th className="px-4 py-3 font-semibold">Current Metrics</th>
                  <th className="px-4 py-3 font-semibold w-1/3">Pending Suggestions</th>
                  <th className="px-4 py-3 font-semibold text-right">Actions</th>
                </tr>
              </thead>
              <tbody>
                {products.length > 0 ? (
                  products.map(product => (
                    <ProductRow 
                      key={product.id} 
                      product={product} 
                      pricingSuggestion={pricingSuggestions[product.id]}
                      reorderSuggestion={reorderSuggestions[product.id]}
                      onSimulateSale={handleSimulateSale}
                      onAcceptPricing={(id) => handlePricingAction(id, 'ACCEPTED')}
                      onRejectPricing={(id) => handlePricingAction(id, 'REJECTED')}
                      onAcceptReorder={(id) => handleReorderAction(id, 'ACCEPTED')}
                      onRejectReorder={(id) => handleReorderAction(id, 'REJECTED')}
                    />
                  ))
                ) : (
                  <tr>
                    <td colSpan="4" className="px-4 py-8 text-center text-sm text-gray-500 font-mono">
                      No products available.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  );
};