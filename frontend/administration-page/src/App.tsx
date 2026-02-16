import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ToastProvider } from './context/ToastContext';
import { AuthGuard } from './components/AuthGuard';
import { AdminLayout } from './components/Layout/AdminLayout';
import { LoginPage } from './pages/LoginPage';
import { EnumsPage } from './pages/EnumsPage';
import { ToursPage } from './pages/ToursPage';
import { PhotoSubmissionsPage } from './pages/PhotoSubmissionsPage';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 60 * 1000,
      retry: 1,
    },
  },
});

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <ToastProvider>
      <BrowserRouter>
        <Routes>
          <Route path="login" element={<LoginPage />} />
          <Route
            element={
              <AuthGuard>
                <AdminLayout />
              </AuthGuard>
            }
          >
            <Route index element={<Navigate to="/tours" replace />} />
            <Route path="tours" element={<ToursPage />} />
            <Route path="photo-submissions" element={<PhotoSubmissionsPage />} />
            <Route path="enums" element={<EnumsPage />} />
            <Route path="*" element={<Navigate to="/tours" replace />} />
          </Route>
        </Routes>
      </BrowserRouter>
      </ToastProvider>
    </QueryClientProvider>
  );
}

export default App;
