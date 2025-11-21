import { useMemo } from 'react';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import type { Evaluation } from '../types';

interface PassRateChartProps {
  evaluations: Evaluation[];
}

/**
 * Animated bar chart showing pass rate by judge.
 */
export function PassRateChart({ evaluations }: PassRateChartProps) {
  const chartData = useMemo(() => {
    // Group evaluations by judge
    const judgeStats: Record<string, { total: number; pass: number }> = {};

    evaluations.forEach((evaluation) => {
      if (!judgeStats[evaluation.judgeId]) {
        judgeStats[evaluation.judgeId] = { total: 0, pass: 0 };
      }
      judgeStats[evaluation.judgeId].total++;
      if (evaluation.verdict === 'PASS') {
        judgeStats[evaluation.judgeId].pass++;
      }
    });

    // Convert to chart data format
    return Object.entries(judgeStats).map(([judgeId, stats]) => ({
      judgeId,
      passRate: stats.total > 0 ? (stats.pass / stats.total) * 100 : 0,
      total: stats.total,
    }));
  }, [evaluations]);

  if (chartData.length === 0) {
    return (
      <div className="text-center py-8 text-gray-500">
        No data available for chart
      </div>
    );
  }

  return (
    <ResponsiveContainer width="100%" height={300}>
      <BarChart data={chartData}>
        <CartesianGrid strokeDasharray="3 3" />
        <XAxis dataKey="judgeId" />
        <YAxis domain={[0, 100]} label={{ value: 'Pass Rate (%)', angle: -90, position: 'insideLeft' }} />
        <Tooltip
          formatter={(value: number) => [`${value.toFixed(1)}%`, 'Pass Rate']}
        />
        <Legend />
        <Bar dataKey="passRate" fill="#3b82f6" name="Pass Rate (%)" />
      </BarChart>
    </ResponsiveContainer>
  );
}

