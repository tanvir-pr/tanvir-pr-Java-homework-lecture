(function () {
    const canvas = document.getElementById('rateChart');
    if (!canvas || typeof Chart === 'undefined') {
        return;
    }
    if (!Array.isArray(chartLabels) || chartLabels.length === 0) {
        return;
    }

    new Chart(canvas, {
        type: 'line',
        data: {
            labels: chartLabels,
            datasets: [{
                label: (typeof chartCurrency !== 'undefined' ? chartCurrency : 'Rate') + ' / HUF',
                data: chartValues,
                borderColor: '#18bfef',
                backgroundColor: 'rgba(24, 191, 239, 0.15)',
                fill: true,
                tension: 0.25,
                pointRadius: 3
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                x: {
                    ticks: { color: '#a0a0a0' },
                    grid: { color: 'rgba(255,255,255,0.08)' }
                },
                y: {
                    ticks: { color: '#a0a0a0' },
                    grid: { color: 'rgba(255,255,255,0.08)' }
                }
            },
            plugins: {
                legend: {
                    labels: { color: '#ffffff' }
                }
            }
        }
    });
})();
