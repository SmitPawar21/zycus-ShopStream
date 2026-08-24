import React, { useState } from 'react';

// --- Stock Heatmap Indicator ---
const StockHeatmap = ({ stockLevel, reorderThreshold }) => {
  const ratio = reorderThreshold > 0 ? stockLevel / reorderThreshold : 1;
  let barColor = 'bg-green-500';
  let label = 'Healthy';

  if (stockLevel === 0) {
    barColor = 'bg-red-600';
    label = 'Out of Stock';
  } else if (ratio <= 0.5) {
    barColor = 'bg-red-500';
    label = 'Critical';
  } else if (ratio <= 1.0) {
    barColor = 'bg-amber-500';
    label = 'Low';
  } else if (ratio <= 2.0) {
    barColor = 'bg-yellow-400';
    label = 'Watch';
  }

  const widthPercent = Math.min(ratio * 33.3, 100); // 3x threshold = full bar

  return (
    <div className="mt-1">
      <div className="flex justify-between items-center mb-0.5">
        <span className="text-[10px] text-gray-500">{label}</span>
        <span className="text-[10px] text-gray-400">{stockLevel}/{reorderThreshold}</span>
      </div>
      <div className="w-full h-1.5 bg-gray-200 rounded-sm overflow-hidden">
        <div className={`h-full ${barColor} rounded-sm`} style={{ width: `${widthPercent}%` }} />
      </div>
    </div>
  );
};

// --- Status Badge ---
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

// --- Suggestion Block ---
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
      
      {isPricing && suggestion.recommendedPrice != null && (
        <div className="text-xs font-mono text-gray-700 mb-1">
          ${suggestion.currentPrice?.toFixed(2)} → <strong>${suggestion.recommendedPrice?.toFixed(2)}</strong>
          <span className={`ml-1.5 text-[10px] font-bold px-1 py-0.5 rounded-sm ${
            suggestion.changeDirection === 'INCREASE' ? 'bg-green-100 text-green-700' :
            suggestion.changeDirection === 'DECREASE' ? 'bg-red-100 text-red-700' :
            'bg-gray-100 text-gray-600'
          }`}>{suggestion.changeDirection}</span>
        </div>
      )}

      {!isPricing && suggestion.recommendedQuantity != null && (
        <div className="text-xs font-mono text-gray-700 mb-1">
          Reorder <strong>{suggestion.recommendedQuantity} units</strong>
          {suggestion.suggestedLeadTimeDays && (
            <span className="text-gray-400 ml-1">({suggestion.suggestedLeadTimeDays}d lead)</span>
          )}
        </div>
      )}
      
      <div className="text-xs text-gray-600 mb-1 leading-tight">
        {suggestion.reasoning}
      </div>
      
      <div className="flex items-center justify-between mt-2">
        <div className="text-xs text-gray-500">
          Confidence: <strong>{(suggestion.confidence * 100).toFixed(0)}%</strong>
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

// --- Price History Mini Table ---
const PriceHistory = ({ history }) => {
  if (!history || history.length === 0) {
    return <div className="text-[10px] text-gray-400 italic mt-1">No price history</div>;
  }

  return (
    <div className="mt-1.5">
      <div className="text-[10px] text-gray-500 font-semibold uppercase mb-1">Recent Price Changes</div>
      <div className="space-y-0.5">
        {history.slice(0, 3).map((entry, idx) => (
          <div key={idx} className="flex justify-between items-center text-[10px] font-mono">
            <span className="text-gray-500">${entry.currentPrice?.toFixed(2)} → ${entry.recommendedPrice?.toFixed(2)}</span>
            <span className={`px-1 py-0 rounded-sm font-bold ${
              entry.status === 'ACCEPTED' ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'
            }`}>{entry.status}</span>
          </div>
        ))}
      </div>
    </div>
  );
};

// --- Main ProductRow ---
export const ProductRow = ({ 
  product, 
  pricingSuggestion, 
  reorderSuggestion, 
  priceHistory,
  onSimulateSale, 
  onAcceptPricing, 
  onRejectPricing, 
  onAcceptReorder, 
  onRejectReorder,
  onUpdateStock,
  onGeneratePricing,
  onGenerateReorder
}) => {
  const [showHistory, setShowHistory] = useState(false);

  // Margin calculation
  const costPrice = product.costPrice || 0;
  const currentPrice = product.currentPrice || 0;
  const margin = currentPrice - costPrice;
  const marginPercent = costPrice > 0 ? ((margin / costPrice) * 100).toFixed(1) : '—';
  const marginColor = margin > 0 ? 'text-green-700' : margin < 0 ? 'text-red-700' : 'text-gray-600';

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
        <div className="grid grid-cols-3 gap-x-4 gap-y-2 text-sm">
          {/* Price */}
          <div>
            <span className="text-xs text-gray-500 block">Price</span>
            <span className="font-medium text-gray-900">${currentPrice.toFixed(2)}</span>
          </div>
          {/* Margin */}
          <div>
            <span className="text-xs text-gray-500 block">Margin</span>
            <span className={`font-medium ${marginColor}`}>{marginPercent}%</span>
            <span className={`text-[10px] block ${marginColor}`}>${margin.toFixed(2)}</span>
          </div>
          {/* Velocity */}
          <div>
            <span className="text-xs text-gray-500 block">Velocity</span>
            <span className="font-medium text-gray-900">{product.demandVelocity}/day</span>
          </div>
          {/* Stock + Heatmap */}
          <div className="col-span-2">
            <span className="text-xs text-gray-500 block">Stock Level</span>
            <span className="font-medium text-gray-900">{product.stockLevel}</span>
            <StockHeatmap stockLevel={product.stockLevel} reorderThreshold={product.reorderThreshold} />
          </div>
          {/* Status */}
          <div>
            <span className="text-xs text-gray-500 block">Status</span>
            <StatusBadge status={product.lifecycleStatus} />
          </div>
        </div>
        {/* Price History Toggle */}
        <button 
          onClick={() => setShowHistory(!showHistory)}
          className="text-[10px] text-indigo-600 hover:text-indigo-800 mt-2 font-medium"
        >
          {showHistory ? '▾ Hide Price History' : '▸ Price History'}
        </button>
        {showHistory && <PriceHistory history={priceHistory} />}
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
