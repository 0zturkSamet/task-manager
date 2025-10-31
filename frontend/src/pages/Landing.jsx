import { Link } from 'react-router-dom';
import { CheckCircle, Users, Calendar, BarChart } from 'lucide-react';
import Button from '../components/common/Button';

const Landing = () => {
  const features = [
    {
      icon: CheckCircle,
      title: 'Task Management',
      description: 'Organize and track your tasks efficiently with our intuitive interface.',
    },
    {
      icon: Users,
      title: 'Team Collaboration',
      description: 'Work together with your team and stay synchronized on projects.',
    },
    {
      icon: Calendar,
      title: 'Project Planning',
      description: 'Plan and manage projects with deadlines and priorities.',
    },
    {
      icon: BarChart,
      title: 'Progress Tracking',
      description: 'Monitor progress with Kanban boards and visual analytics.',
    },
  ];

  return (
    <div className="min-h-screen bg-gradient-to-br from-primary-50 to-primary-100">
      {/* Hero Section */}
      <div className="container mx-auto px-4 py-16">
        <div className="text-center mb-16">
          <h1 className="text-5xl md:text-6xl font-bold text-gray-900 mb-6">
            Task Manager
          </h1>
          <p className="text-xl md:text-2xl text-gray-700 mb-8 max-w-3xl mx-auto">
            Streamline your workflow, collaborate with your team, and achieve your goals with our powerful task management platform.
          </p>
          <div className="flex flex-col sm:flex-row gap-4 justify-center items-center">
            <Link to="/register">
              <Button variant="primary" size="lg" className="w-full sm:w-auto px-8 py-3 text-lg">
                Get Started
              </Button>
            </Link>
            <Link to="/login">
              <Button variant="secondary" size="lg" className="w-full sm:w-auto px-8 py-3 text-lg">
                Sign In
              </Button>
            </Link>
          </div>
        </div>

        {/* Features Grid */}
        <div className="grid md:grid-cols-2 lg:grid-cols-4 gap-8 mt-16">
          {features.map((feature, index) => {
            const Icon = feature.icon;
            return (
              <div
                key={index}
                className="bg-white p-6 rounded-lg shadow-md hover:shadow-lg transition-shadow"
              >
                <div className="flex items-center justify-center w-12 h-12 bg-primary-100 rounded-lg mb-4">
                  <Icon className="w-6 h-6 text-primary-600" />
                </div>
                <h3 className="text-xl font-semibold text-gray-900 mb-2">
                  {feature.title}
                </h3>
                <p className="text-gray-600">
                  {feature.description}
                </p>
              </div>
            );
          })}
        </div>

        {/* Call to Action */}
        <div className="mt-16 text-center">
          <div className="bg-white rounded-lg shadow-lg p-8 max-w-2xl mx-auto">
            <h2 className="text-3xl font-bold text-gray-900 mb-4">
              Ready to boost your productivity?
            </h2>
            <p className="text-gray-600 mb-6">
              Join thousands of teams already using Task Manager to streamline their work.
            </p>
            <Link to="/register">
              <Button variant="primary" size="lg" className="px-8 py-3 text-lg">
                Create Free Account
              </Button>
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Landing;
