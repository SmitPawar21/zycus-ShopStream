import React from 'react';
import { SuggestionPanel } from './SuggestionPanel';

export const ProductCard = ({ 
  product, 
  pricingSuggestion, 
  reorderSuggestion, 
  onSimulateSale, 
  onAcceptPricing, 
  onRejectPricing, 
  onAcceptReorder, 
  onRejectReorder 
}) => {
  return (
    <div className="bg-white shadow rounded-lg p-6 flex flex-col h-full border border-gray-100">
      <div className="flex justify-between items-start mb-4">
        <div>
          <h3 className="text-xl font-bold text-gray-900">{product.name}</h3>
          <p className="text-sm text-gray-500">SKU: {product.sku} | Category: {product.category}</p>
        </div>
        <span className={`px-2 py-1 text-xs font-semibold rounded-full ${
          product.lifecycleStatus === 'OUT_OF_STOCK' ? 'bg-red-100 text-red-800' : 
          product.lifecycleStatus === 'PRICE_REVIEW_PENDING' ? 'bg-yellow-100 text-yellow-800' :
          'bg-green-100 text-green-800'
        }`}>
          {product.lifecycleStatus}
        </span>
      </div>
      
      <div className="grid grid-cols-2 gap-4 mb-4 text-sm">
        <div>
          <p className="text-gray-500 font-medium">Price</p>
          <p className="text-gray-900 text-lg">${product.currentPrice.toFixed(2)}</p>
        </div>
        <div>
          <p className="text-gray-500 font-medium">Stock</p>
          <p className="text-gray-900 text-lg">{product.stockLevel}</p>
        </div>
        <div>
          <p className="text-gray-500 font-medium">Demand Velocity</p>
          <p className="text-gray-900 text-lg">{product.demandVelocity}/day</p>
        </div>
        <div>
          <p className="text-gray-500 font-medium">Reorder Thresh</p>
          <p className="text-gray-900 text-lg">{product.reorderThreshold}</p>
        </div>
      </div>
      
      <div className="mt-2 mb-4">
        <button 
          onClick={() => onSimulateSale(product.id)}
          className="w-full bg-indigo-600 hover:bg-indigo-700 text-white font-bold py-2 px-4 rounded text-sm transition-colors"
        >
          Simulate Sale
        </button>
      </div>
      
      <div className="mt-auto space-y-4">
        <SuggestionPanel 
          type="pricing" 
          suggestion={pricingSuggestion} 
          onAccept={onAcceptPricing} 
          onReject={onRejectPricing} 
        />
        <SuggestionPanel 
          type="reorder" 
          suggestion={reorderSuggestion} 
          onAccept={onAcceptReorder} 
          onReject={onRejectReorder} 
        />
      </div>
    </div>
  );
};
