import { useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import {
  LayoutDashboard,
  FolderKanban,
  CheckSquare,
  Columns,
  Menu,
  X
} from 'lucide-react';

const Sidebar = () => {
  const location = useLocation();
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);

  const navItems = [
    {
      path: '/dashboard',
      icon: LayoutDashboard,
      label: 'Dashboard',
      color: '#4285f4', // Google Blue
      bgColor: '#e8f0fe'
    },
    {
      path: '/projects',
      icon: FolderKanban,
      label: 'Projects',
      color: '#ff9800', // Material Orange
      bgColor: '#fff3e0'
    },
    {
      path: '/tasks',
      icon: CheckSquare,
      label: 'Tasks',
      color: '#34a853', // Google Green
      bgColor: '#e6f4ea'
    },
    {
      path: '/kanban',
      icon: Columns,
      label: 'Kanban Board',
      color: '#9c27b0', // Material Purple
      bgColor: '#f3e5f5'
    },
  ];

  const isActive = (path) => location.pathname === path;

  const closeMobileMenu = () => setIsMobileMenuOpen(false);

  return (
    <>
      {/* Mobile Menu Button */}
      <button
        onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
        className="lg:hidden fixed top-4 left-4 z-50 p-2 bg-white rounded-lg shadow-md hover:bg-secondary-100 smooth-transition"
        aria-label="Toggle menu"
      >
        {isMobileMenuOpen ? (
          <X className="w-6 h-6 text-secondary-700" />
        ) : (
          <Menu className="w-6 h-6 text-secondary-700" />
        )}
      </button>

      {/* Mobile Overlay */}
      {isMobileMenuOpen && (
        <div
          className="lg:hidden fixed inset-0 bg-black/50 z-30"
          onClick={closeMobileMenu}
        />
      )}

      {/* Sidebar */}
      <aside
        className={`
          fixed lg:static inset-y-0 left-0 z-40
          w-64 bg-white border-r border-secondary-200 min-h-screen flex flex-col shadow-lg lg:shadow-sm
          transform transition-transform duration-300 ease-smooth
          ${isMobileMenuOpen ? 'translate-x-0' : '-translate-x-full lg:translate-x-0'}
        `}
      >
        {/* Header */}
        <div className="p-6 border-b border-secondary-100">
          <h1 className="text-2xl font-bold text-primary-600 tracking-tight">Task Manager</h1>
          <p className="text-xs text-secondary-500 mt-1.5 font-medium">Organize & Track</p>
        </div>

        {/* Navigation */}
        <nav className="flex-1 p-4">
          <ul className="space-y-2">
            {navItems.map((item) => {
              const Icon = item.icon;
              const active = isActive(item.path);
              return (
                <li key={item.path}>
                  <Link
                    to={item.path}
                    onClick={closeMobileMenu}
                    className={`nav-item ${
                      active ? 'nav-item-active' : 'nav-item-inactive'
                    }`}
                  >
                    <div
                      className="w-9 h-9 rounded-lg flex items-center justify-center mr-3 smooth-transition"
                      style={{
                        backgroundColor: active ? item.color : item.bgColor
                      }}
                    >
                      <Icon
                        className="w-5 h-5 smooth-transition"
                        style={{ color: active ? '#ffffff' : item.color }}
                      />
                    </div>
                    <span>{item.label}</span>
                  </Link>
                </li>
              );
            })}
          </ul>
        </nav>
      </aside>
    </>
  );
};

export default Sidebar;
