const nextConfig = {
  eslint: {
    ignoreDuringBuilds: true,
  },
  typescript: {
    ignoreBuildErrors: true,
  },
  images: {
    unoptimized: true,
  },
  async rewrites() {
    return [
      {
        source: '/api/v1/form/:storeId',
        destination: 'http://localhost:8080/api/v1/form/:storeId',
      },
      {
        source: '/api/bookings',
        destination: 'http://localhost:8080/api/bookings',
      },
      {
        source: '/api/v1/:path*',
        destination: 'http://localhost:8080/api/v1/:path*',
      },
    ]
  },
}

export default nextConfig
