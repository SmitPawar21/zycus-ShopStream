import React, { useState, useEffect, useCallback } from 'react';
import { 
  fetchProducts, 
  fetchPendingPricingSuggestions, 
  fetchPendingReorderSuggestions, 
  simulateSale, 
  updatePricingSuggestion, 
  updateReorderSuggestion 
} from '../api';
import { ProductCard } from '../components/ProductCard';

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
      
      // Index suggestions by productId for easy lookup
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
      loadData(); // Refresh immediately
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
    return <div className="p-8 text-center text-gray-500">Loading merchandise data...</div>;
  }

  return (
    <div className="min-h-screen bg-gray-50 p-6 font-sans">
      <div className="max-w-7xl mx-auto">
        <header className="mb-8">
          <h1 className="text-3xl font-extrabold text-gray-900 tracking-tight">T-5 Merchandising Console</h1>
          <p className="text-gray-500 mt-2">Manage products, view metrics, and resolve AI suggestions.</p>
        </header>

        {error && (
          <div className="bg-red-50 border-l-4 border-red-400 p-4 mb-6">
            <div className="flex">
              <div className="ml-3">
                <p className="text-sm text-red-700">{error}</p>
              </div>
            </div>
          </div>
        )}

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {products.map(product => (
            <ProductCard 
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
          ))}
        </div>
        
        {products.length === 0 && !loading && !error && (
          <div className="text-center text-gray-500 mt-12 bg-white p-12 rounded-lg border border-gray-200">
            No products found. Start by seeding the database or creating a product.
          </div>
        )}
      </div>
    </div>
  );
};