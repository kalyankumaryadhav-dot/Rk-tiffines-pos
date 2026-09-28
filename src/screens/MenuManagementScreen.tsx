import React, { useState, useMemo } from 'react';
import {
  ArrowDown,
  ArrowUp,
  Edit2,
  Plus,
  RotateCcw,
  Search,
  Trash2,
  UtensilsCrossed,
  X,
} from 'lucide-react';
import { usePos } from '../context/PosContext';
import { DepartmentInfo, MenuItem } from '../types/pos';

export const MenuManagementScreen: React.FC = () => {
  const {
    departments,
    menuItems,
    addMenuItem,
    updateMenuItem,
    deleteMenuItem,
    toggleItemAvailability,
    addDepartment,
    renameDepartment,
    deleteDepartment,
    reorderDepartments,
    resetToDefaultMenu,
  } = usePos();

  const [selectedDeptIndex, setSelectedDeptIndex] = useState(0);
  const [searchQuery, setSearchQuery] = useState('');

  // Department Modal states
  const [editDepartmentsModalOpen, setEditDepartmentsModalOpen] = useState(false);
  const [addDeptModalOpen, setAddDeptModalOpen] = useState(false);
  const [editingDept, setEditingDept] = useState<DepartmentInfo | null>(null);
  const [deletingDept, setDeletingDept] = useState<DepartmentInfo | null>(null);

  // Item Modal states
  const [addItemModalOpen, setAddItemModalOpen] = useState(false);
  const [editingItem, setEditingItem] = useState<MenuItem | null>(null);
  const [deletingItem, setDeletingItem] = useState<MenuItem | null>(null);

  const safeDeptIndex = Math.min(selectedDeptIndex, Math.max(0, departments.length - 1));
  const currentDepartment = departments[safeDeptIndex];
  const currentCategory = currentDepartment?.name || '';

  // Search filtering
  const isSearching = searchQuery.trim().length > 0;
  const displayedItems = useMemo(() => {
    if (isSearching) {
      const q = searchQuery.toLowerCase().trim();
      return menuItems.filter(
        (it) => it.name.toLowerCase().includes(q) || it.category.toLowerCase().includes(q)
      );
    }
    return menuItems.filter((it) => it.category.toUpperCase() === currentCategory.toUpperCase());
  }, [menuItems, searchQuery, isSearching, currentCategory]);

  return (
    <div className="flex-1 flex flex-col p-3 sm:p-4 bg-[#0C0B0A] overflow-y-auto max-w-6xl mx-auto w-full">
      {/* 1. Header */}
      <div className="flex flex-wrap items-center justify-between gap-3 mb-3">
        <div>
          <h2 className="text-lg sm:text-xl font-extrabold text-[#F9F6F0]">
            Menu & Department Management
          </h2>
          <p className="text-xs text-[#B8B0A6]">
            {departments.length} Departments • {menuItems.length} items in RK TIFFINES menu
          </p>
        </div>

        <div className="flex items-center gap-2">
          <button
            onClick={() => setAddItemModalOpen(true)}
            className="flex items-center gap-1.5 px-3 py-2 rounded-lg bg-[#FFB300] text-[#140D00] font-bold text-xs hover:brightness-110 active:scale-95 transition-all shadow-sm"
          >
            <Plus className="w-4 h-4 font-bold" />
            <span>Add Item</span>
          </button>

          <button
            onClick={() => {
              if (window.confirm('Reset menu to official 12 departments and 115 RK Tiffines items?')) {
                resetToDefaultMenu();
              }
            }}
            className="flex items-center gap-1.5 px-2.5 py-2 rounded-lg border border-[#FFB300]/30 text-[#B8B0A6] hover:text-[#F9F6F0] hover:bg-[#22201D] active:scale-95 transition-all text-xs"
            title="Reset to official 115 items"
          >
            <RotateCcw className="w-3.5 h-3.5" />
            <span className="hidden sm:inline">Reset Menu</span>
          </button>
        </div>
      </div>

      {/* 2. Search & Edit Departments Button */}
      <div className="flex flex-col sm:flex-row items-stretch sm:items-center gap-2.5 mb-3">
        <div className="relative flex-1">
          <Search className="w-4 h-4 text-[#FFB300] absolute left-3 top-1/2 -translate-y-1/2" />
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            placeholder="Search menu items or departments…"
            className="w-full bg-[#161514] border border-[#FFB300]/25 rounded-xl pl-9 pr-8 py-2 text-xs sm:text-sm text-[#F9F6F0] placeholder-[#6E6760] focus:outline-none focus:border-[#FFB300]"
          />
          {searchQuery && (
            <button
              onClick={() => setSearchQuery('')}
              className="absolute right-2.5 top-1/2 -translate-y-1/2 text-[#6E6760] hover:text-[#F9F6F0]"
            >
              <X className="w-4 h-4" />
            </button>
          )}
        </div>

        <button
          onClick={() => setEditDepartmentsModalOpen(true)}
          className="flex items-center justify-center gap-2 px-4 py-2 rounded-xl bg-[#161514] border border-[#FFB300] text-[#FFB300] hover:bg-[#FFB300]/10 text-xs sm:text-sm font-bold active:scale-95 transition-all shrink-0"
        >
          <Edit2 className="w-4 h-4" />
          <span>✏ Edit Departments</span>
        </button>
      </div>

      {/* 3. Scrollable Department Tabs (when not searching) */}
      {!isSearching && (
        <div className="overflow-x-auto no-scrollbar pb-1 mb-3 flex items-center gap-2">
          {departments.map((dept, index) => {
            const isSelected = safeDeptIndex === index;
            return (
              <button
                key={dept.id}
                onClick={() => setSelectedDeptIndex(index)}
                className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg shrink-0 text-xs font-semibold transition-all active:scale-95 ${
                  isSelected
                    ? 'bg-[#FFB300] text-[#140D00] shadow-sm font-bold'
                    : 'bg-[#161514] text-[#F9F6F0] border border-[#FFB300]/20 hover:bg-[#22201D]'
                }`}
              >
                <span
                  className={`text-[10px] font-bold px-1 rounded ${
                    isSelected ? 'bg-black/15 text-[#140D00]' : 'bg-[#22201D] text-[#FFB300]'
                  }`}
                >
                  {dept.code}
                </span>
                <span>{dept.name}</span>
              </button>
            );
          })}
        </div>
      )}

      {/* 4. Category header / match counter */}
      <div className="flex items-center justify-between mb-2">
        <h3 className="font-bold text-xs sm:text-sm text-[#FFB300]">
          {isSearching
            ? `Results for "${searchQuery}" (${displayedItems.length} items)`
            : `${currentCategory} (${displayedItems.length} items)`}
        </h3>
      </div>

      {/* 5. Menu Items List */}
      <div className="flex-1 space-y-2">
        {displayedItems.length === 0 ? (
          <div className="bg-[#161514] border border-[#FFB300]/20 rounded-xl p-8 text-center text-[#B8B0A6]">
            <UtensilsCrossed className="w-10 h-10 text-[#6E6760] mx-auto mb-2" />
            <p className="text-sm font-medium">
              {isSearching
                ? `No items matching "${searchQuery}"`
                : `No items in ${currentCategory}. Click "+ Add Item" to add one.`}
            </p>
          </div>
        ) : (
          displayedItems.map((item) => (
            <div
              key={item.id}
              className="bg-[#161514] border border-[#FFB300]/20 hover:border-[#FFB300]/40 rounded-xl p-3 flex items-center justify-between gap-3 transition-all"
            >
              {/* Item Details */}
              <div className="flex-1 min-w-0">
                <div className="flex items-center gap-2 flex-wrap">
                  <span className="font-bold text-sm text-[#F9F6F0] truncate">
                    {item.name}
                  </span>
                  {isSearching && (
                    <span className="px-2 py-0.5 rounded bg-[#22201D] border border-[#FFB300]/30 text-[10px] font-bold text-[#FFB300]">
                      {item.category}
                    </span>
                  )}
                </div>

                <p className="text-xs font-extrabold text-[#FFC107] my-0.5">
                  Rate: ₹ {item.price.toFixed(0)}
                </p>

                <p
                  className={`text-[11px] font-medium ${
                    item.isAvailable ? 'text-emerald-400' : 'text-red-400'
                  }`}
                >
                  {item.isAvailable ? 'In Stock (Available for billing)' : 'Out of Stock (Hidden)'}
                </p>
              </div>

              {/* Actions: Toggle, Edit, Delete */}
              <div className="flex items-center gap-2 shrink-0">
                {/* Switch */}
                <label className="relative inline-flex items-center cursor-pointer">
                  <input
                    type="checkbox"
                    checked={item.isAvailable}
                    onChange={() => toggleItemAvailability(item)}
                    className="sr-only peer"
                  />
                  <div className="w-9 h-5 bg-[#22201D] border border-[#FFB300]/30 rounded-full peer peer-checked:bg-[#FFB300] peer-checked:after:translate-x-full after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-[#140D00] after:rounded-full after:h-4 after:w-4 after:transition-all" />
                </label>

                <button
                  onClick={() => setEditingItem(item)}
                  className="p-1.5 rounded-lg text-[#FFB300] hover:bg-[#22201D] active:scale-95 transition-all"
                  title="Edit item"
                >
                  <Edit2 className="w-4 h-4" />
                </button>

                <button
                  onClick={() => setDeletingItem(item)}
                  className="p-1.5 rounded-lg text-red-400 hover:bg-[#22201D] active:scale-95 transition-all"
                  title="Delete item"
                >
                  <Trash2 className="w-4 h-4" />
                </button>
              </div>
            </div>
          ))
        )}
      </div>

      {/* ------------------------------------------------------------------ */}
      {/* Edit Departments Modal */}
      {/* ------------------------------------------------------------------ */}
      {editDepartmentsModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-3 bg-black/80 backdrop-blur-sm">
          <div className="relative w-full max-w-xl bg-[#161514] border border-[#FFB300]/30 rounded-2xl shadow-2xl p-4 flex flex-col max-h-[90vh] overflow-hidden">
            {/* Header */}
            <div className="flex items-center justify-between pb-3 border-b border-[#FFB300]/20">
              <div>
                <h3 className="font-bold text-base text-[#F9F6F0]">✏ Edit Departments</h3>
                <p className="text-xs text-[#B8B0A6]">
                  Manage department names, reorder positions, or add categories
                </p>
              </div>
              <button
                onClick={() => setEditDepartmentsModalOpen(false)}
                className="p-1 rounded-lg text-[#B8B0A6] hover:text-[#F9F6F0] hover:bg-[#2B2824]"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            {/* Add Department CTA */}
            <div className="py-2.5">
              <button
                onClick={() => setAddDeptModalOpen(true)}
                className="w-full py-2 px-3 rounded-lg bg-[#FFB300] text-[#140D00] font-bold text-xs flex items-center justify-center gap-1.5 hover:brightness-110 active:scale-95 transition-all"
              >
                <Plus className="w-4 h-4 font-bold" />
                <span>+ Add New Department</span>
              </button>
            </div>

            {/* Departments List */}
            <div className="flex-1 overflow-y-auto space-y-2 py-1 pr-1">
              {departments.map((dept, index) => {
                const count = menuItems.filter(
                  (m) => m.category.toUpperCase() === dept.name.toUpperCase()
                ).length;

                return (
                  <div
                    key={dept.id}
                    className="flex items-center justify-between p-2.5 rounded-lg bg-[#22201D] border border-[#FFB300]/20 text-xs gap-2"
                  >
                    <div className="flex items-center gap-2 min-w-0">
                      <span className="w-5 h-5 rounded bg-[#161514] text-[#B8B0A6] font-bold flex items-center justify-center shrink-0">
                        {index + 1}
                      </span>
                      <span className="px-1.5 py-0.5 rounded bg-[#FFB300] text-[#140D00] font-bold shrink-0">
                        {dept.code}
                      </span>
                      <div className="min-w-0">
                        <p className="font-bold text-[#F9F6F0] truncate">{dept.name}</p>
                        <p className="text-[11px] text-[#B8B0A6]">{count} items assigned</p>
                      </div>
                    </div>

                    <div className="flex items-center gap-1 shrink-0">
                      <button
                        disabled={index === 0}
                        onClick={() => {
                          if (index > 0) {
                            const copy = [...departments];
                            const temp = copy[index];
                            copy[index] = copy[index - 1];
                            copy[index - 1] = temp;
                            reorderDepartments(copy);
                          }
                        }}
                        className="p-1.5 rounded hover:bg-[#161514] text-[#FFB300] disabled:opacity-30"
                        title="Move Up"
                      >
                        <ArrowUp className="w-3.5 h-3.5" />
                      </button>

                      <button
                        disabled={index === departments.length - 1}
                        onClick={() => {
                          if (index < departments.length - 1) {
                            const copy = [...departments];
                            const temp = copy[index];
                            copy[index] = copy[index + 1];
                            copy[index + 1] = temp;
                            reorderDepartments(copy);
                          }
                        }}
                        className="p-1.5 rounded hover:bg-[#161514] text-[#FFB300] disabled:opacity-30"
                        title="Move Down"
                      >
                        <ArrowDown className="w-3.5 h-3.5" />
                      </button>

                      <button
                        onClick={() => setEditingDept(dept)}
                        className="p-1.5 rounded hover:bg-[#161514] text-[#FFB300]"
                        title="Edit Department"
                      >
                        <Edit2 className="w-3.5 h-3.5" />
                      </button>

                      <button
                        onClick={() => setDeletingDept(dept)}
                        className="p-1.5 rounded hover:bg-[#161514] text-red-400"
                        title="Delete Department"
                      >
                        <Trash2 className="w-3.5 h-3.5" />
                      </button>
                    </div>
                  </div>
                );
              })}
            </div>

            {/* Footer */}
            <div className="pt-3 border-t border-[#FFB300]/20 flex justify-end">
              <button
                onClick={() => setEditDepartmentsModalOpen(false)}
                className="px-4 py-2 rounded-lg bg-[#22201D] border border-[#FFB300]/30 text-[#FFB300] font-bold text-xs hover:bg-[#2B2824]"
              >
                Done
              </button>
            </div>
          </div>
        </div>
      )}

      {/* ------------------------------------------------------------------ */}
      {/* Add / Edit Department Dialog */}
      {/* ------------------------------------------------------------------ */}
      {(addDeptModalOpen || editingDept) && (
        <DepartmentDialog
          title={editingDept ? 'Edit Department' : 'Add New Department'}
          initialName={editingDept?.name || ''}
          initialCode={editingDept?.code || ''}
          onClose={() => {
            setAddDeptModalOpen(false);
            setEditingDept(null);
          }}
          onSave={(name, code) => {
            if (editingDept) {
              renameDepartment(editingDept.name, name, code);
            } else {
              addDepartment(name, code);
            }
            setAddDeptModalOpen(false);
            setEditingDept(null);
          }}
        />
      )}

      {/* Delete Department Confirmation */}
      {deletingDept && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-3 bg-black/80 backdrop-blur-sm">
          <div className="w-full max-w-sm bg-[#161514] border border-red-500/30 rounded-2xl p-4 space-y-3">
            <h3 className="font-bold text-base text-[#F9F6F0]">
              Delete Department "{deletingDept.name}"?
            </h3>
            <p className="text-xs text-[#B8B0A6]">
              Are you sure you want to remove this department? Its existing menu items will be preserved safely.
            </p>
            <div className="flex justify-end gap-2 pt-2">
              <button
                onClick={() => setDeletingDept(null)}
                className="px-3 py-1.5 rounded-lg border border-[#B8B0A6]/30 text-xs text-[#B8B0A6] hover:bg-[#22201D]"
              >
                Cancel
              </button>
              <button
                onClick={() => {
                  deleteDepartment(deletingDept);
                  setDeletingDept(null);
                }}
                className="px-3 py-1.5 rounded-lg bg-red-600 text-white font-bold text-xs hover:bg-red-700"
              >
                Delete
              </button>
            </div>
          </div>
        </div>
      )}

      {/* ------------------------------------------------------------------ */}
      {/* Add / Edit Item Dialog */}
      {/* ------------------------------------------------------------------ */}
      {(addItemModalOpen || editingItem) && (
        <ItemDialog
          title={editingItem ? 'Edit Menu Item' : 'Add New Item to Menu'}
          initialName={editingItem?.name || ''}
          initialCategory={editingItem?.category || currentCategory}
          initialPrice={editingItem ? editingItem.price.toString() : ''}
          departments={departments}
          onClose={() => {
            setAddItemModalOpen(false);
            setEditingItem(null);
          }}
          onSave={(name, category, price) => {
            if (editingItem) {
              updateMenuItem({
                ...editingItem,
                name,
                category,
                price,
              });
            } else {
              addMenuItem(name, category, price);
            }
            setAddItemModalOpen(false);
            setEditingItem(null);
          }}
        />
      )}

      {/* Delete Item Confirmation */}
      {deletingItem && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-3 bg-black/80 backdrop-blur-sm">
          <div className="w-full max-w-sm bg-[#161514] border border-red-500/30 rounded-2xl p-4 space-y-3">
            <h3 className="font-bold text-base text-[#F9F6F0]">
              Delete "{deletingItem.name}"?
            </h3>
            <p className="text-xs text-[#B8B0A6]">
              Are you sure you want to remove this item from the menu?
            </p>
            <div className="flex justify-end gap-2 pt-2">
              <button
                onClick={() => setDeletingItem(null)}
                className="px-3 py-1.5 rounded-lg border border-[#B8B0A6]/30 text-xs text-[#B8B0A6] hover:bg-[#22201D]"
              >
                Cancel
              </button>
              <button
                onClick={() => {
                  deleteMenuItem(deletingItem.id);
                  setDeletingItem(null);
                }}
                className="px-3 py-1.5 rounded-lg bg-red-600 text-white font-bold text-xs hover:bg-red-700"
              >
                Delete
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

// Department Dialog Component
interface DepartmentDialogProps {
  title: string;
  initialName: string;
  initialCode: string;
  onClose: () => void;
  onSave: (name: string, code: string) => void;
}

const DepartmentDialog: React.FC<DepartmentDialogProps> = ({
  title,
  initialName,
  initialCode,
  onClose,
  onSave,
}) => {
  const [name, setName] = useState(initialName);
  const [code, setCode] = useState(initialCode);
  const [error, setError] = useState('');

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim()) {
      setError('Please enter a department name');
      return;
    }
    onSave(name.trim().toUpperCase(), code.trim().toUpperCase());
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-3 bg-black/80 backdrop-blur-sm">
      <form
        onSubmit={handleSubmit}
        className="w-full max-w-md bg-[#161514] border border-[#FFB300]/30 rounded-2xl p-4 space-y-3"
      >
        <h3 className="font-bold text-base text-[#F9F6F0]">{title}</h3>

        <div>
          <label className="text-xs text-[#B8B0A6] font-medium block mb-1">Department Name</label>
          <input
            type="text"
            value={name}
            onChange={(e) => setName(e.target.value)}
            placeholder="e.g. DOSA ITEMS, MAGGI"
            className="w-full bg-[#22201D] border border-[#FFB300]/30 rounded-lg px-3 py-2 text-xs sm:text-sm text-[#F9F6F0] focus:outline-none focus:border-[#FFB300]"
          />
        </div>

        <div>
          <label className="text-xs text-[#B8B0A6] font-medium block mb-1">
            Short Code (Optional)
          </label>
          <input
            type="text"
            value={code}
            onChange={(e) => setCode(e.target.value)}
            placeholder="e.g. DI, MG"
            className="w-full bg-[#22201D] border border-[#FFB300]/30 rounded-lg px-3 py-2 text-xs sm:text-sm text-[#F9F6F0] focus:outline-none focus:border-[#FFB300]"
          />
        </div>

        {error && <p className="text-xs text-red-400 font-medium">{error}</p>}

        <div className="flex justify-end gap-2 pt-2">
          <button
            type="button"
            onClick={onClose}
            className="px-3 py-1.5 rounded-lg border border-[#B8B0A6]/30 text-xs text-[#B8B0A6] hover:bg-[#22201D]"
          >
            Cancel
          </button>
          <button
            type="submit"
            className="px-4 py-1.5 rounded-lg bg-[#FFB300] text-[#140D00] font-bold text-xs hover:brightness-110"
          >
            Save
          </button>
        </div>
      </form>
    </div>
  );
};

// Item Dialog Component
interface ItemDialogProps {
  title: string;
  initialName: string;
  initialCategory: string;
  initialPrice: string;
  departments: DepartmentInfo[];
  onClose: () => void;
  onSave: (name: string, category: string, price: number) => void;
}

const ItemDialog: React.FC<ItemDialogProps> = ({
  title,
  initialName,
  initialCategory,
  initialPrice,
  departments,
  onClose,
  onSave,
}) => {
  const [name, setName] = useState(initialName);
  const [category, setCategory] = useState(initialCategory || departments[0]?.name || 'SOUTH INDIAN');
  const [price, setPrice] = useState(initialPrice);
  const [error, setError] = useState('');

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim()) {
      setError('Please enter item name');
      return;
    }
    const numPrice = Number(price);
    if (isNaN(numPrice) || numPrice < 0) {
      setError('Please enter a valid price');
      return;
    }
    onSave(name.trim().toUpperCase(), category.toUpperCase(), numPrice);
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-3 bg-black/80 backdrop-blur-sm">
      <form
        onSubmit={handleSubmit}
        className="w-full max-w-md bg-[#161514] border border-[#FFB300]/30 rounded-2xl p-4 space-y-3"
      >
        <h3 className="font-bold text-base text-[#F9F6F0]">{title}</h3>

        <div>
          <label className="text-xs text-[#B8B0A6] font-medium block mb-1">Item Name</label>
          <input
            type="text"
            value={name}
            onChange={(e) => setName(e.target.value)}
            placeholder="e.g. GHEE MASALA DOSA"
            className="w-full bg-[#22201D] border border-[#FFB300]/30 rounded-lg px-3 py-2 text-xs sm:text-sm text-[#F9F6F0] focus:outline-none focus:border-[#FFB300]"
          />
        </div>

        <div>
          <label className="text-xs text-[#B8B0A6] font-medium block mb-1">Department</label>
          <select
            value={category}
            onChange={(e) => setCategory(e.target.value)}
            className="w-full bg-[#22201D] border border-[#FFB300]/30 rounded-lg px-3 py-2 text-xs sm:text-sm text-[#F9F6F0] focus:outline-none focus:border-[#FFB300]"
          >
            {departments.map((d) => (
              <option key={d.id} value={d.name}>
                {d.name} ({d.code})
              </option>
            ))}
          </select>
        </div>

        <div>
          <label className="text-xs text-[#B8B0A6] font-medium block mb-1">Price (₹)</label>
          <input
            type="number"
            step="1"
            value={price}
            onChange={(e) => setPrice(e.target.value)}
            placeholder="e.g. 60"
            className="w-full bg-[#22201D] border border-[#FFB300]/30 rounded-lg px-3 py-2 text-xs sm:text-sm text-[#F9F6F0] focus:outline-none focus:border-[#FFB300]"
          />
        </div>

        {error && <p className="text-xs text-red-400 font-medium">{error}</p>}

        <div className="flex justify-end gap-2 pt-2">
          <button
            type="button"
            onClick={onClose}
            className="px-3 py-1.5 rounded-lg border border-[#B8B0A6]/30 text-xs text-[#B8B0A6] hover:bg-[#22201D]"
          >
            Cancel
          </button>
          <button
            type="submit"
            className="px-4 py-1.5 rounded-lg bg-[#FFB300] text-[#140D00] font-bold text-xs hover:brightness-110"
          >
            Save Item
          </button>
        </div>
      </form>
    </div>
  );
};
