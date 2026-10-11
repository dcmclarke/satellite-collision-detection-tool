import { render, screen, fireEvent } from '@testing-library/react';
import SatelliteList from './SatelliteList';
import { satelliteApi } from '../services/api';

// replace the real API module, so the tests never make HTTP requests
jest.mock('../services/api', () => ({
  satelliteApi: {
    getAllSatellites: jest.fn(),
    loadBackupData: jest.fn(),
    detectCollisions: jest.fn(),
    fetchSpaceTrackData: jest.fn(),
  },
}));

const satellites = [
  { id: 1, name: 'ISS (ZARYA)', noradId: '25544', latitude: 51.64, longitude: 247.46, altitude: 1550.1 },
  { id: 2, name: 'STARLINK-1007', noradId: '44713', latitude: 53.05, longitude: 327.85, altitude: 1506.42 },
];

test('shows the satellites returned by the API', async () => {
  satelliteApi.getAllSatellites.mockResolvedValue({ data: satellites });

  render(<SatelliteList />);

  expect(await screen.findByText('ISS (ZARYA)')).toBeInTheDocument();
  expect(screen.getByText('STARLINK-1007')).toBeInTheDocument();
  expect(screen.getByText('Loaded Satellites (2)')).toBeInTheDocument();
});

test('search filters the table by name or NORAD ID', async () => {
  satelliteApi.getAllSatellites.mockResolvedValue({ data: satellites });
  render(<SatelliteList />);
  await screen.findByText('ISS (ZARYA)');

  fireEvent.change(screen.getByPlaceholderText('Search by name or NORAD ID...'), { target: { value: '44713' } });

  expect(screen.queryByText('ISS (ZARYA)')).not.toBeInTheDocument();
  expect(screen.getByText('STARLINK-1007')).toBeInTheDocument();
});

test('shows the message from the backend when a request is refused', async () => {
  satelliteApi.getAllSatellites.mockResolvedValue({ data: [] });
  // the shape axios gives for an error response: the JSON body is in error.response.data
  satelliteApi.loadBackupData.mockRejectedValue({
    message: 'Request failed with status code 403',
    response: { status: 403, data: { message: 'Manual data refresh is turned off on this server.' } },
  });
  render(<SatelliteList />);
  await screen.findByText('No Satellite Data Loaded');

  fireEvent.click(screen.getByText('Load Backup Data'));

  expect(await screen.findByText('Error: Manual data refresh is turned off on this server.')).toBeInTheDocument();
});
