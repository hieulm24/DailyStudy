// Lightweight zero-dependency HTML5 Canvas Confetti Engine
export function triggerConfetti(options?: { duration?: number; particleCount?: number }) {
  if (typeof window === 'undefined' || typeof document === 'undefined') return;

  const duration = options?.duration || 3000;
  const count = options?.particleCount || 100;
  const colors = ['#6366f1', '#ec4899', '#f59e0b', '#10b981', '#3b82f6', '#8b5cf6', '#ef4444', '#14b8a6'];

  const canvas = document.createElement('canvas');
  canvas.style.position = 'fixed';
  canvas.style.top = '0';
  canvas.style.left = '0';
  canvas.style.width = '100vw';
  canvas.style.height = '100vh';
  canvas.style.pointerEvents = 'none';
  canvas.style.zIndex = '99999';
  document.body.appendChild(canvas);

  const ctx = canvas.getContext('2d');
  if (!ctx) {
    document.body.removeChild(canvas);
    return;
  }

  let width = (canvas.width = window.innerWidth);
  let height = (canvas.height = window.innerHeight);

  const handleResize = () => {
    width = canvas.width = window.innerWidth;
    height = canvas.height = window.innerHeight;
  };
  window.addEventListener('resize', handleResize);

  interface Particle {
    x: number;
    y: number;
    vx: number;
    vy: number;
    size: number;
    color: string;
    rotation: number;
    rotationSpeed: number;
    shape: 'rect' | 'circle' | 'star';
    opacity: number;
  }

  const particles: Particle[] = [];

  for (let i = 0; i < count; i++) {
    const angle = (Math.PI / 4) + Math.random() * (Math.PI / 2); // Upwards arc
    const speed = 8 + Math.random() * 18;
    const originX = Math.random() > 0.5 ? width * 0.2 : width * 0.8;

    particles.push({
      x: originX + (Math.random() - 0.5) * 100,
      y: height * 0.85,
      vx: (Math.random() - 0.5) * speed * 1.5,
      vy: -Math.sin(angle) * speed * 1.2,
      size: 6 + Math.random() * 8,
      color: colors[Math.floor(Math.random() * colors.length)],
      rotation: Math.random() * 360,
      rotationSpeed: (Math.random() - 0.5) * 12,
      shape: Math.random() > 0.6 ? 'circle' : (Math.random() > 0.4 ? 'star' : 'rect'),
      opacity: 1,
    });
  }

  const startTime = Date.now();

  function render() {
    if (!ctx) return;
    const elapsed = Date.now() - startTime;
    const progress = elapsed / duration;

    ctx.clearRect(0, 0, width, height);

    if (progress >= 1) {
      window.removeEventListener('resize', handleResize);
      if (canvas.parentNode) {
        document.body.removeChild(canvas);
      }
      return;
    }

    particles.forEach((p) => {
      p.x += p.vx;
      p.y += p.vy;
      p.vy += 0.35; // Gravity
      p.vx *= 0.99; // Air drag
      p.rotation += p.rotationSpeed;
      p.opacity = Math.max(0, 1 - progress * 1.2);

      ctx.save();
      ctx.translate(p.x, p.y);
      ctx.rotate((p.rotation * Math.PI) / 180);
      ctx.globalAlpha = p.opacity;
      ctx.fillStyle = p.color;

      if (p.shape === 'circle') {
        ctx.beginPath();
        ctx.arc(0, 0, p.size / 2, 0, Math.PI * 2);
        ctx.fill();
      } else if (p.shape === 'star') {
        ctx.beginPath();
        for (let j = 0; j < 5; j++) {
          ctx.lineTo(Math.cos(((18 + j * 72) * Math.PI) / 180) * p.size, -Math.sin(((18 + j * 72) * Math.PI) / 180) * p.size);
          ctx.lineTo(Math.cos(((54 + j * 72) * Math.PI) / 180) * (p.size / 2), -Math.sin(((54 + j * 72) * Math.PI) / 180) * (p.size / 2));
        }
        ctx.closePath();
        ctx.fill();
      } else {
        ctx.fillRect(-p.size / 2, -p.size / 2, p.size, p.size * 0.6);
      }

      ctx.restore();
    });

    requestAnimationFrame(render);
  }

  requestAnimationFrame(render);
}
