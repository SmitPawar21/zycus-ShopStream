import React, { useState, useEffect, useCallback } from 'react';
import { 
  fetchProducts, 
  fetchPendingPricingSuggestions, 
  fetchPendingReorderSuggestions, 
  simulateSale, 
  updatePricingSuggestion, 
  updateReorderSuggestion,
  fetchAvailableStrategies,
  fetchActiveStrategies,
  activatePricingStrategy,
  activateReorderStrategy,
  generatePricingSuggestion,
  generateReorderSuggestion,
  updateStock,
  createProduct
} from '../api';
import { ProductRow } from '../components/ProductRow';

export const HomePage = () => {
  const [products, setProducts] = useState([]);
  const [pricingSuggestions, setPricingSuggestions] = useState({});
  const [reorderSuggestions, setReorderSuggestions] = useState({});
  const [availableStrategies, setAvailableStrategies] = useState({ pricingStrategies: [], reorderStrategies: [] });
  const [activeStrategies, setActiveStrategies] = useState({ pricingStrategy: '', reorderStrategy: '' });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  
  const loadData = useCallback(async () => {
    try {
      const [productsData, pricingData, reorderData, availableData, activeData] = await Promise.all([
        fetchProducts(),
        fetchPendingPricingSuggestions(),
        fetchPendingReorderSuggestions(),
        fetchAvailableStrategies(),
        fetchActiveStrategies()
      ]);
      
      setProducts(productsData);
      setAvailableStrategies(availableData);
      setActiveStrategies(activeData);
      
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

  const handleUpdateStock = async (productId) => {
    const newStock = window.prompt('Enter new stock level:');
    if (newStock !== null && !isNaN(newStock) && newStock.trim() !== '') {
      try {
        await updateStock(productId, newStock);
        loadData();
      } catch (err) {
        alert('Stock update failed: ' + err.message);
      }
    }
  };

  const handleGeneratePricing = async (productId) => {
    try {
      await generatePricingSuggestion(productId);
      loadData();
    } catch (err) {
      alert('Failed to generate pricing suggestion: ' + err.message);
    }
  };

  const handleGenerateReorder = async (productId) => {
    try {
      await generateReorderSuggestion(productId);
      loadData();
    } catch (err) {
      alert('Failed to generate reorder suggestion: ' + err.message);
    }
  };

  const handleAddProduct = async () => {
    const sku = window.prompt('SKU (e.g., ELEC-005):');
    if (!sku) return;
    const name = window.prompt('Name:');
    if (!name) return;
    const category = window.prompt('Category (ELECTRONICS, APPAREL, HOME):');
    if (!category) return;
    const currentPrice = window.prompt('Current Price:');
    if (!currentPrice) return;
    const reorderThreshold = window.prompt('Reorder Threshold:');
    if (!reorderThreshold) return;

    try {
      await createProduct({
        sku,
        name,
        category,
        currentPrice: parseFloat(currentPrice),
        stockLevel: 0,
        reorderThreshold: parseInt(reorderThreshold, 10),
        costPrice: parseFloat(currentPrice) * 0.5 // Default cost
      });
      loadData();
    } catch (err) {
      alert('Failed to create product: ' + err.message);
    }
  };

  const handleChangePricingStrategy = async (e) => {
    const newStrategy = e.target.value;
    try {
      await activatePricingStrategy(newStrategy);
      loadData();
    } catch (err) {
      alert('Failed to activate strategy: ' + err.message);
    }
  };

  const handleChangeReorderStrategy = async (e) => {
    const newStrategy = e.target.value;
    try {
      await activateReorderStrategy(newStrategy);
      loadData();
    } catch (err) {
      alert('Failed to activate strategy: ' + err.message);
    }
  };

  if (loading && products.length === 0) {
    return <div className="p-8 text-sm text-gray-500 font-mono">Loading data...</div>;
  }

  return (
    <div className="min-h-screen bg-gray-100 p-4 sm:p-6 lg:p-8 font-sans">
      <div className="max-w-[1400px] mx-auto">
        
        {/* Header Section */}
        <header className="mb-4 flex flex-col md:flex-row md:justify-between md:items-end border-b border-gray-300 pb-4">
          <div>
            <h1 className="text-2xl font-bold text-gray-900 tracking-tight">Merchandising Operations Console</h1>
            <p className="text-sm text-gray-600 mt-1 font-mono">T-5 Systems // AI Inventory & Pricing Engine</p>
          </div>
          <div className="mt-4 md:mt-0 flex items-center space-x-4">
            <div className="text-xs text-gray-500 font-mono hidden md:block">
              {products.length} Products
            </div>
            <button 
              onClick={handleAddProduct}
              className="bg-indigo-600 hover:bg-indigo-700 text-white font-medium py-1.5 px-4 rounded-sm text-sm"
            >
              + Add Product
            </button>
          </div>
        </header>

        {/* Strategy Management Section */}
        <div className="bg-white border border-gray-300 shadow-sm rounded-sm p-4 mb-6 flex flex-wrap gap-6 items-center">
          <div className="text-sm font-semibold text-gray-800 mr-2">System AI Configuration:</div>
          
          <div className="flex items-center space-x-2">
            <label className="text-xs font-medium text-gray-600">Pricing Strategy:</label>
            <select 
              value={activeStrategies.pricingStrategy} 
              onChange={handleChangePricingStrategy}
              className="text-sm border border-gray-300 rounded-sm px-2 py-1 bg-gray-50 text-gray-800"
            >
              {availableStrategies.pricingStrategies?.map(s => (
                <option key={s} value={s}>{s}</option>
              ))}
            </select>
          </div>

          <div className="flex items-center space-x-2">
            <label className="text-xs font-medium text-gray-600">Reorder Strategy:</label>
            <select 
              value={activeStrategies.reorderStrategy} 
              onChange={handleChangeReorderStrategy}
              className="text-sm border border-gray-300 rounded-sm px-2 py-1 bg-gray-50 text-gray-800"
            >
              {availableStrategies.reorderStrategies?.map(s => (
                <option key={s} value={s}>{s}</option>
              ))}
            </select>
          </div>
        </div>

        {error && (
          <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-sm text-sm mb-6">
            <strong>System Error:</strong> {error}
          </div>
        )}

        {/* Products Table */}
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
                      onUpdateStock={() => handleUpdateStock(product.id)}
                      onGeneratePricing={() => handleGeneratePricing(product.id)}
                      onGenerateReorder={() => handleGenerateReorder(product.id)}
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