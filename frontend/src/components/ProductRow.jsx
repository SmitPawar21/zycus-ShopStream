import React from 'react';

const StatusBadge = ({ status }) => {
  let bgColor = 'bg-gray-100';
  let textColor = 'text-gray-800';
  
  if (status === 'ACTIVE') {
    bgColor = 'bg-green-100';
    textColor = 'text-green-800';
  } else if (status === 'PRICE_REVIEW_PENDING') {
    bgColor = 'bg-yellow-100';
    textColor = 'text-yellow-800';
  } else if (status === 'OUT_OF_STOCK') {
    bgColor = 'bg-red-100';
    textColor = 'text-red-800';
  }

  return (
    <span className={`inline-block px-2 py-0.5 text-xs font-medium rounded-sm ${bgColor} ${textColor}`}>
      {status}
    </span>
  );
};

const SuggestionBlock = ({ type, suggestion, onAccept, onReject }) => {
  if (!suggestion) return null;

  const isPricing = type === 'pricing';
  
  return (
    <div className="border-l-2 border-indigo-500 pl-3 py-1 mb-3 last:mb-0">
      <div className="flex justify-between items-baseline mb-1">
        <span className="text-xs font-bold text-gray-900">
          {isPricing ? 'Pricing Suggestion' : 'Reorder Suggestion'}
        </span>
        <span className="text-[10px] font-bold px-1.5 py-0.5 bg-gray-200 text-gray-700 rounded-sm">
          {suggestion.triggerReason}
        </span>
      </div>
      
      <div className="text-xs text-gray-800 mb-1 leading-tight">
        {suggestion.reasoning}
      </div>
      
      <div className="flex items-center justify-between mt-2">
        <div className="text-xs text-gray-600">
          Confidence: {(suggestion.confidence * 100).toFixed(0)}%
        </div>
        <div className="flex gap-1.5">
          <button 
            onClick={() => onAccept(suggestion.id)}
            className="px-2 py-1 bg-green-600 text-white text-xs font-medium rounded-sm hover:bg-green-700"
          >
            Accept
          </button>
          <button 
            onClick={() => onReject(suggestion.id)}
            className="px-2 py-1 bg-red-600 text-white text-xs font-medium rounded-sm hover:bg-red-700"
          >
            Reject
          </button>
        </div>
      </div>
    </div>
  );
};

export const ProductRow = ({ 
  product, 
  pricingSuggestion, 
  reorderSuggestion, 
  onSimulateSale, 
  onAcceptPricing, 
  onRejectPricing, 
  onAcceptReorder, 
  onRejectReorder,
  onUpdateStock,
  onGeneratePricing,
  onGenerateReorder
}) => {
  return (
    <tr className="border-b border-gray-200 hover:bg-gray-50">
      {/* Product Details */}
      <td className="px-4 py-3 align-top">
        <div className="text-sm font-bold text-gray-900">{product.name}</div>
        <div className="text-xs text-gray-500 mt-0.5 font-mono">{product.sku}</div>
        <div className="text-xs text-gray-500 mt-0.5">{product.category}</div>
      </td>
      
      {/* Metrics */}
      <td className="px-4 py-3 align-top">
        <div className="grid grid-cols-2 gap-x-4 gap-y-2 text-sm">
          <div>
            <span className="text-xs text-gray-500 block">Price</span>
            <span className="font-medium text-gray-900">${product.currentPrice.toFixed(2)}</span>
          </div>
          <div>
            <span className="text-xs text-gray-500 block">Stock</span>
            <span className="font-medium text-gray-900">{product.stockLevel}</span>
            <span className="text-xs text-gray-400 ml-1">(Thresh: {product.reorderThreshold})</span>
          </div>
          <div>
            <span className="text-xs text-gray-500 block">Velocity</span>
            <span className="font-medium text-gray-900">{product.demandVelocity}/day</span>
          </div>
          <div>
            <span className="text-xs text-gray-500 block">Status</span>
            <StatusBadge status={product.lifecycleStatus} />
          </div>
        </div>
      </td>
      
      {/* Suggestions */}
      <td className="px-4 py-3 align-top bg-gray-50/50">
        {!pricingSuggestion && !reorderSuggestion ? (
          <span className="text-xs text-gray-400 italic">No pending suggestions</span>
        ) : (
          <div className="flex flex-col">
            <SuggestionBlock 
              type="pricing" 
              suggestion={pricingSuggestion} 
              onAccept={onAcceptPricing} 
              onReject={onRejectPricing} 
            />
            <SuggestionBlock 
              type="reorder" 
              suggestion={reorderSuggestion} 
              onAccept={onAcceptReorder} 
              onReject={onRejectReorder} 
            />
          </div>
        )}
      </td>
      
      {/* Actions */}
      <td className="px-4 py-3 align-top text-right border-l border-gray-100">
        <div className="flex flex-col space-y-2 w-32 ml-auto">
          <button 
            onClick={() => onSimulateSale(product.id)}
            className="px-2 py-1 border border-gray-300 text-gray-700 text-xs font-medium rounded-sm hover:bg-gray-100"
          >
            Simulate Sale
          </button>
          <button 
            onClick={() => onUpdateStock(product.id)}
            className="px-2 py-1 border border-gray-300 text-gray-700 text-xs font-medium rounded-sm hover:bg-gray-100"
          >
            Update Stock
          </button>
          <div className="border-t border-gray-200 my-1 pt-1 flex flex-col space-y-1">
            <span className="text-[10px] text-gray-400 uppercase text-center mb-1">Generate AI</span>
            <button 
              onClick={() => onGeneratePricing(product.id)}
              className="px-2 py-1 border border-indigo-200 bg-indigo-50 text-indigo-700 text-xs font-medium rounded-sm hover:bg-indigo-100"
            >
              Pricing
            </button>
            <button 
              onClick={() => onGenerateReorder(product.id)}
              className="px-2 py-1 border border-indigo-200 bg-indigo-50 text-indigo-700 text-xs font-medium rounded-sm hover:bg-indigo-100"
            >
              Reorder
            </button>
          </div>
        </div>
      </td>
    </tr>
  );
};
