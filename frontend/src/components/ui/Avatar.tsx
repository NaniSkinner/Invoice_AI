import React from 'react';

interface AvatarProps {
  name: string;
  size?: 'sm' | 'md' | 'lg' | 'xl';
  imageUrl?: string;
  className?: string;
}

export const Avatar: React.FC<AvatarProps> = ({ 
  name, 
  size = 'md', 
  imageUrl, 
  className = '' 
}) => {
  const sizeStyles = {
    sm: 'h-8 w-8 text-xs',
    md: 'h-10 w-10 text-sm',
    lg: 'h-12 w-12 text-base',
    xl: 'h-16 w-16 text-xl',
  };

  // Generate initials from name (first letter of first two words)
  const getInitials = (name: string) => {
    const words = name.trim().split(' ');
    if (words.length >= 2) {
      return `${words[0][0]}${words[1][0]}`.toUpperCase();
    }
    return name.substring(0, 2).toUpperCase();
  };

  // Generate a consistent color based on the name
  const getColorFromName = (name: string) => {
    const colors = [
      'bg-primary-500 text-white',
      'bg-emerald-500 text-white',
      'bg-blue-500 text-white',
      'bg-purple-500 text-white',
      'bg-pink-500 text-white',
      'bg-orange-500 text-white',
      'bg-cyan-500 text-white',
      'bg-indigo-500 text-white',
      'bg-teal-500 text-white',
      'bg-rose-500 text-white',
    ];
    
    // Use name length and first char code to pick a color
    const index = (name.length + name.charCodeAt(0)) % colors.length;
    return colors[index];
  };

  const initials = getInitials(name);
  const colorClass = getColorFromName(name);

  if (imageUrl) {
    return (
      <div className={`${sizeStyles[size]} rounded-full overflow-hidden ${className}`}>
        <img 
          src={imageUrl} 
          alt={name} 
          className="h-full w-full object-cover"
        />
      </div>
    );
  }

  return (
    <div 
      className={`${sizeStyles[size]} ${colorClass} rounded-full flex items-center justify-center font-semibold shadow-sm ${className}`}
    >
      {initials}
    </div>
  );
};

