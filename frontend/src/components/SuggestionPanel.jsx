import React from 'react';

export const SuggestionPanel = ({ type, suggestion, onAccept, onReject }) => {
  if (!suggestion) return null;

  const isPricing = type === 'pricing';
  
  return (
    <div className="border border-gray-200 rounded p-4 mt-4 bg-gray-50">
      <div className="flex justify-between items-start mb-2">
        <h4 className="text-md font-semibold text-gray-800">
          {isPricing ? 'Pending Pricing Suggestion' : 'Pending Reorder Suggestion'}
        </h4>
        <span className="bg-blue-100 text-blue-800 text-xs font-semibold px-2.5 py-0.5 rounded">
          Trigger: {suggestion.triggerReason}
        </span>
      </div>
      
      <div className="text-sm text-gray-600 mb-3">
        <p><strong>Reasoning:</strong> {suggestion.reasoning}</p>
        <p><strong>Confidence:</strong> {(suggestion.confidence * 100).toFixed(0)}%</p>
      </div>
      
      <div className="flex space-x-2">
        <button 
          onClick={() => onAccept(suggestion.id)}
          className="bg-green-600 hover:bg-green-700 text-white font-bold py-1 px-3 rounded text-sm"
        >
          Accept
        </button>
        <button 
          onClick={() => onReject(suggestion.id)}
          className="bg-red-600 hover:bg-red-700 text-white font-bold py-1 px-3 rounded text-sm"
        >
          Reject
        </button>
      </div>
    </div>
  );
};
