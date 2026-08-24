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
  createProduct,
  fetchAllPricingSuggestions
} from '../api';
import { ProductRow } from '../components/ProductRow';

// --- Notification Component ---
const Notification = ({ message, type, onClose }) => {
  if (!message) return null;
  const isError = type === 'error';
  return (
    <div className={`fixed bottom-4 right-4 z-50 px-4 py-3 rounded-sm shadow-lg flex items-center justify-between min-w-[300px] border ${isError ? 'bg-red-50 border-red-200 text-red-700' : 'bg-green-50 border-green-200 text-green-700'}`}>
      <span className="text-sm font-medium">{message}</span>
      <button onClick={onClose} className="ml-4 text-gray-500 hover:text-gray-700 font-bold">&times;</button>
    </div>
  );
};

// --- Modals ---
const AddProductModal = ({ isOpen, onClose, onSubmit }) => {
  const [formData, setFormData] = useState({ sku: '', name: '', category: 'ELECTRONICS', currentPrice: '', reorderThreshold: '' });

  if (!isOpen) return null;

  const handleSubmit = (e) => {
    e.preventDefault();
    onSubmit(formData);
  };

  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-40">
      <div className="bg-white rounded-sm shadow-xl p-6 w-full max-w-md">
        <h2 className="text-lg font-bold mb-4">Add New Product</h2>
        <form onSubmit={handleSubmit} className="space-y-4 text-sm">
          <div>
            <label className="block text-gray-700 font-medium mb-1">SKU</label>
            <input required type="text" value={formData.sku} onChange={e => setFormData({...formData, sku: e.target.value})} className="w-full border border-gray-300 rounded-sm px-3 py-2" placeholder="e.g., ELEC-005" />
          </div>
          <div>
            <label className="block text-gray-700 font-medium mb-1">Name</label>
            <input required type="text" value={formData.name} onChange={e => setFormData({...formData, name: e.target.value})} className="w-full border border-gray-300 rounded-sm px-3 py-2" />
          </div>
          <div>
            <label className="block text-gray-700 font-medium mb-1">Category</label>
            <select value={formData.category} onChange={e => setFormData({...formData, category: e.target.value})} className="w-full border border-gray-300 rounded-sm px-3 py-2 bg-white">
              <option value="ELECTRONICS">ELECTRONICS</option>
              <option value="APPAREL">APPAREL</option>
              <option value="HOME">HOME</option>
            </select>
          </div>
          <div>
            <label className="block text-gray-700 font-medium mb-1">Current Price</label>
            <input required type="number" step="0.01" min="0" value={formData.currentPrice} onChange={e => setFormData({...formData, currentPrice: e.target.value})} className="w-full border border-gray-300 rounded-sm px-3 py-2" />
          </div>
          <div>
            <label className="block text-gray-700 font-medium mb-1">Reorder Threshold</label>
            <input required type="number" min="0" value={formData.reorderThreshold} onChange={e => setFormData({...formData, reorderThreshold: e.target.value})} className="w-full border border-gray-300 rounded-sm px-3 py-2" />
          </div>
          <div className="flex justify-end space-x-3 mt-6">
            <button type="button" onClick={onClose} className="px-4 py-2 border border-gray-300 text-gray-700 rounded-sm hover:bg-gray-50">Cancel</button>
            <button type="submit" className="px-4 py-2 bg-indigo-600 text-white rounded-sm hover:bg-indigo-700">Save Product</button>
          </div>
        </form>
      </div>
    </div>
  );
};

const UpdateStockModal = ({ isOpen, onClose, onSubmit, productId }) => {
  const [stockLevel, setStockLevel] = useState('');

  if (!isOpen) return null;

  const handleSubmit = (e) => {
    e.preventDefault();
    onSubmit(productId, stockLevel);
    setStockLevel('');
  };

  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-40">
      <div className="bg-white rounded-sm shadow-xl p-6 w-full max-w-sm">
        <h2 className="text-lg font-bold mb-4">Update Stock Level</h2>
        <form onSubmit={handleSubmit} className="space-y-4 text-sm">
          <div>
            <label className="block text-gray-700 font-medium mb-1">New Stock Level</label>
            <input required type="number" min="0" value={stockLevel} onChange={e => setStockLevel(e.target.value)} className="w-full border border-gray-300 rounded-sm px-3 py-2" autoFocus />
          </div>
          <div className="flex justify-end space-x-3 mt-6">
            <button type="button" onClick={onClose} className="px-4 py-2 border border-gray-300 text-gray-700 rounded-sm hover:bg-gray-50">Cancel</button>
            <button type="submit" className="px-4 py-2 bg-indigo-600 text-white rounded-sm hover:bg-indigo-700">Update Stock</button>
          </div>
        </form>
      </div>
    </div>
  );
};

export const HomePage = () => {
  const [products, setProducts] = useState([]);
  const [pricingSuggestions, setPricingSuggestions] = useState({});
  const [reorderSuggestions, setReorderSuggestions] = useState({});
  const [allPricingHistory, setAllPricingHistory] = useState({});
  const [availableStrategies, setAvailableStrategies] = useState({ pricingStrategies: [], reorderStrategies: [] });
  const [activeStrategies, setActiveStrategies] = useState({ pricingStrategy: '', reorderStrategy: '' });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  
  // UI States
  const [categoryFilter, setCategoryFilter] = useState('ALL');
  const [notification, setNotification] = useState(null);
  const [isAddModalOpen, setIsAddModalOpen] = useState(false);
  const [stockModalData, setStockModalData] = useState({ isOpen: false, productId: null });

  const showNotification = (message, type = 'success') => {
    setNotification({ message, type });
    setTimeout(() => setNotification(null), 3000);
  };

  const loadData = useCallback(async () => {
    try {
      const [productsData, pricingData, reorderData, availableData, activeData, allPricingData] = await Promise.all([
        fetchProducts(),
        fetchPendingPricingSuggestions(),
        fetchPendingReorderSuggestions(),
        fetchAvailableStrategies(),
        fetchActiveStrategies(),
        fetchAllPricingSuggestions()
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

      // Group pricing history by product ID
      const historyMap = {};
      allPricingData.forEach(s => {
        if (!historyMap[s.productId]) historyMap[s.productId] = [];
        historyMap[s.productId].push(s);
      });
      // Sort history descending by ID (assuming higher ID = newer)
      Object.keys(historyMap).forEach(key => {
        historyMap[key].sort((a, b) => b.id - a.id);
      });
      setAllPricingHistory(historyMap);
      
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
      showNotification('Sale simulated successfully');
    } catch (err) {
      showNotification('Simulation failed: ' + err.message, 'error');
    }
  };
  
  const handlePricingAction = async (id, action) => {
    try {
      await updatePricingSuggestion(id, action);
      loadData();
      showNotification(`Pricing suggestion ${action.toLowerCase()}`);
    } catch (err) {
      showNotification(`Pricing action failed: ` + err.message, 'error');
    }
  };
  
  const handleReorderAction = async (id, action) => {
    try {
      await updateReorderSuggestion(id, action);
      loadData();
      showNotification(`Reorder suggestion ${action.toLowerCase()}`);
    } catch (err) {
      showNotification(`Reorder action failed: ` + err.message, 'error');
    }
  };

  const handleUpdateStock = async (productId, newStock) => {
    if (newStock !== null && !isNaN(newStock) && newStock.toString().trim() !== '') {
      try {
        await updateStock(productId, newStock);
        setStockModalData({ isOpen: false, productId: null });
        loadData();
        showNotification('Stock updated successfully');
      } catch (err) {
        showNotification('Stock update failed: ' + err.message, 'error');
      }
    }
  };

  const handleGeneratePricing = async (productId) => {
    try {
      await generatePricingSuggestion(productId);
      loadData();
      showNotification('Pricing suggestion generated');
    } catch (err) {
      showNotification('Failed to generate pricing suggestion: ' + err.message, 'error');
    }
  };

  const handleGenerateReorder = async (productId) => {
    try {
      await generateReorderSuggestion(productId);
      loadData();
      showNotification('Reorder suggestion generated');
    } catch (err) {
      showNotification('Failed to generate reorder suggestion: ' + err.message, 'error');
    }
  };

  const handleAddProduct = async (formData) => {
    try {
      await createProduct({
        ...formData,
        currentPrice: parseFloat(formData.currentPrice),
        stockLevel: 0,
        reorderThreshold: parseInt(formData.reorderThreshold, 10),
        costPrice: parseFloat(formData.currentPrice) * 0.5 // Default cost
      });
      setIsAddModalOpen(false);
      loadData();
      showNotification('Product created successfully');
    } catch (err) {
      showNotification('Failed to create product: ' + err.message, 'error');
    }
  };

  const handleChangePricingStrategy = async (e) => {
    const newStrategy = e.target.value;
    try {
      await activatePricingStrategy(newStrategy);
      loadData();
      showNotification('Pricing strategy updated');
    } catch (err) {
      showNotification('Failed to activate strategy: ' + err.message, 'error');
    }
  };

  const handleChangeReorderStrategy = async (e) => {
    const newStrategy = e.target.value;
    try {
      await activateReorderStrategy(newStrategy);
      loadData();
      showNotification('Reorder strategy updated');
    } catch (err) {
      showNotification('Failed to activate strategy: ' + err.message, 'error');
    }
  };

  const filteredProducts = categoryFilter === 'ALL' 
    ? products 
    : products.filter(p => p.category === categoryFilter);

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
              onClick={() => setIsAddModalOpen(true)}
              className="bg-indigo-600 hover:bg-indigo-700 text-white font-medium py-1.5 px-4 rounded-sm text-sm"
            >
              + Add Product
            </button>
          </div>
        </header>

        {/* Filters and Config Section */}
        <div className="bg-white border border-gray-300 shadow-sm rounded-sm p-4 mb-6 flex flex-wrap gap-x-8 gap-y-4 items-center justify-between">
          
          <div className="flex items-center space-x-2">
            <span className="text-sm font-semibold text-gray-800">Category:</span>
            <select 
              value={categoryFilter}
              onChange={(e) => setCategoryFilter(e.target.value)}
              className="text-sm border border-gray-300 rounded-sm px-2 py-1 bg-gray-50 text-gray-800 focus:outline-none focus:border-indigo-500"
            >
              <option value="ALL">All Categories</option>
              <option value="ELECTRONICS">Electronics</option>
              <option value="APPAREL">Apparel</option>
              <option value="HOME">Home</option>
            </select>
          </div>

          <div className="flex flex-wrap gap-4 items-center">
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
                  <th className="px-4 py-3 font-semibold w-1/3">Current Metrics</th>
                  <th className="px-4 py-3 font-semibold w-1/3">Pending Suggestions</th>
                  <th className="px-4 py-3 font-semibold text-right">Actions</th>
                </tr>
              </thead>
              <tbody>
                {filteredProducts.length > 0 ? (
                  filteredProducts.map(product => (
                    <ProductRow 
                      key={product.id} 
                      product={product} 
                      pricingSuggestion={pricingSuggestions[product.id]}
                      reorderSuggestion={reorderSuggestions[product.id]}
                      priceHistory={allPricingHistory[product.id]}
                      onSimulateSale={handleSimulateSale}
                      onAcceptPricing={(id) => handlePricingAction(id, 'ACCEPTED')}
                      onRejectPricing={(id) => handlePricingAction(id, 'REJECTED')}
                      onAcceptReorder={(id) => handleReorderAction(id, 'ACCEPTED')}
                      onRejectReorder={(id) => handleReorderAction(id, 'REJECTED')}
                      onUpdateStock={() => setStockModalData({ isOpen: true, productId: product.id })}
                      onGeneratePricing={() => handleGeneratePricing(product.id)}
                      onGenerateReorder={() => handleGenerateReorder(product.id)}
                    />
                  ))
                ) : (
                  <tr>
                    <td colSpan="4" className="px-4 py-8 text-center text-sm text-gray-500 font-mono">
                      No products match your filters.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </div>
      </div>

      {/* Global Modals and Notifications */}
      <AddProductModal 
        isOpen={isAddModalOpen} 
        onClose={() => setIsAddModalOpen(false)} 
        onSubmit={handleAddProduct} 
      />
      
      <UpdateStockModal 
        isOpen={stockModalData.isOpen} 
        productId={stockModalData.productId}
        onClose={() => setStockModalData({ isOpen: false, productId: null })} 
        onSubmit={handleUpdateStock} 
      />
      
      <Notification 
        message={notification?.message} 
        type={notification?.type} 
        onClose={() => setNotification(null)} 
      />
    </div>
  );
};