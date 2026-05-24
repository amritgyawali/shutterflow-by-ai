import React from 'react';
import { StyleSheet, Text, View, StatusBar, TouchableOpacity } from 'react-native';

export default function App() {
  return (
    <View style={styles.container}>
      <StatusBar barStyle="light-content" />
      
      {/* Visual Accent Layer */}
      <View style={styles.glow} />
      
      <View style={styles.header}>
        <Text style={styles.logoText}>
          Shutter<Text style={styles.accentText}>Flow</Text>
        </Text>
        <Text style={styles.subtitle}>Mobile Client Portal</Text>
      </View>

      <View style={styles.card}>
        <Text style={styles.cardTitle}>📸 Photographer Pocket Suite</Text>
        <Text style={styles.cardDesc}>
          Access your bookings, track wedding schedules, sign contracts, and check client CRM lists on the go.
        </Text>
        <TouchableOpacity style={styles.button} activeOpacity={0.8}>
          <Text style={styles.buttonText}>Studio Login</Text>
        </TouchableOpacity>
      </View>

      <Text style={styles.footer}>Sprint 1 Setup Complete • Version 1.0.0</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#0b0f19',
    alignItems: 'center',
    justifyContent: 'center',
    padding: 24,
  },
  glow: {
    position: 'absolute',
    width: 300,
    height: 300,
    borderRadius: 150,
    backgroundColor: 'rgba(16, 185, 129, 0.05)',
    top: 50,
    right: -50,
  },
  header: {
    alignItems: 'center',
    marginBottom: 40,
  },
  logoText: {
    fontSize: 36,
    fontWeight: '800',
    color: '#ffffff',
    letterSpacing: -1,
  },
  accentText: {
    color: '#10b981',
  },
  subtitle: {
    fontSize: 14,
    color: '#9ca3af',
    marginTop: 4,
    letterSpacing: 2,
    textTransform: 'uppercase',
  },
  card: {
    backgroundColor: 'rgba(17, 24, 39, 0.7)',
    borderRadius: 20,
    borderWidth: 1,
    borderColor: 'rgba(255, 255, 255, 0.08)',
    padding: 24,
    width: '100%',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 10 },
    shadowOpacity: 0.3,
    shadowRadius: 20,
    elevation: 5,
  },
  cardTitle: {
    fontSize: 18,
    fontWeight: '700',
    color: '#f3f4f6',
    marginBottom: 12,
  },
  cardDesc: {
    fontSize: 14,
    color: '#9ca3af',
    lineHeight: 20,
    marginBottom: 24,
  },
  button: {
    backgroundColor: '#10b981',
    borderRadius: 12,
    paddingVertical: 14,
    alignItems: 'center',
    justifyContent: 'center',
    shadowColor: '#10b981',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.3,
    shadowRadius: 10,
    elevation: 3,
  },
  buttonText: {
    color: '#ffffff',
    fontSize: 16,
    fontWeight: '600',
  },
  footer: {
    position: 'absolute',
    bottom: 40,
    fontSize: 12,
    color: '#6b7280',
  },
});
