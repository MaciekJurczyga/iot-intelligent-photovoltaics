import React, { useState, useRef, useMemo } from 'react';
import { FaChevronLeft, FaChevronRight, FaCalendarAlt } from 'react-icons/fa';
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  ReferenceLine
} from 'recharts';

// Przykładowe dane - struktura gotowa pod backend
interface ChartData {
  name: string;
  production: number;
  consumption: number;
  grid: number; // ujemne dla poboru/wysyłki
}

const ChartsView: React.FC = () => {
  // Stan dla dat - inicjalizacja dzisiejszą datą
  const [dailyDate, setDailyDate] = useState(new Date());
  const [weeklyDate, setWeeklyDate] = useState(new Date());

  // Referencje do ukrytych inputów daty
  const dailyDateInputRef = useRef<HTMLInputElement>(null);
  const weeklyDateInputRef = useRef<HTMLInputElement>(null);

  // --- Funkcje pomocnicze do dat ---

  const formatDate = (date: Date): string => {
    return date.toLocaleDateString('pl-PL', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
    });
  };

  const getWeekRange = (date: Date): string => {
    // Obliczamy początek tygodnia (poniedziałek)
    const start = new Date(date);
    const day = start.getDay();
    const diff = start.getDate() - day + (day === 0 ? -6 : 1); // adjust when day is sunday
    start.setDate(diff);

    // Obliczamy koniec tygodnia (niedziela)
    const end = new Date(start);
    end.setDate(start.getDate() + 6);
    
    const startStr = start.toLocaleDateString('pl-PL', { day: 'numeric' });
    const endStr = end.toLocaleDateString('pl-PL', { day: 'numeric', month: '2-digit', year: 'numeric' });
    
    return `${startStr}-${endStr}`;
  };

  // --- Obsługa zmiany dat ---

  const changeDay = (days: number) => {
    const newDate = new Date(dailyDate);
    newDate.setDate(dailyDate.getDate() + days);
    
    // Blokada przyszłości
    if (newDate > new Date()) return;
    
    setDailyDate(newDate);
  };

  const changeWeek = (weeks: number) => {
    const newDate = new Date(weeklyDate);
    newDate.setDate(weeklyDate.getDate() + (weeks * 7));
    
    // Sprawdzenie czy początek nowego tygodnia nie jest w przyszłości
    // (można pozwolić na bieżący tydzień, nawet jeśli się nie skończył)
    const currentWeekStart = new Date();
    const day = currentWeekStart.getDay();
    const diff = currentWeekStart.getDate() - day + (day === 0 ? -6 : 1);
    currentWeekStart.setDate(diff);
    currentWeekStart.setHours(0,0,0,0);

    // Obliczamy początek nowego tygodnia
    const newWeekStart = new Date(newDate);
    const newDay = newWeekStart.getDay();
    const newDiff = newWeekStart.getDate() - newDay + (newDay === 0 ? -6 : 1);
    newWeekStart.setDate(newDiff);
    newWeekStart.setHours(0,0,0,0);

    if (newWeekStart > currentWeekStart) return;

    setWeeklyDate(newDate);
  };

  const handleDailyDateChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.value) {
      const newDate = new Date(e.target.value);
      if (newDate <= new Date()) {
        setDailyDate(newDate);
      }
    }
  };

  const handleWeeklyDateChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.value) {
      const newDate = new Date(e.target.value);
      // Pozwalamy wybrać datę w bieżącym tygodniu lub przeszłości
      const today = new Date();
      // Znajdź koniec bieżącego tygodnia (niedziela)
      const endOfWeek = new Date(today);
      const day = today.getDay();
      const diff = today.getDate() - day + (day === 0 ? 0 : 7); // next sunday
      endOfWeek.setDate(diff);
      
      if (newDate <= endOfWeek) {
         setWeeklyDate(newDate);
      }
    }
  };

  // --- Generowanie przykładowych danych (do podmiany na API) ---

  const dailyData = useMemo(() => {
    const data: ChartData[] = [];
    const now = new Date();
    const isToday = dailyDate.toDateString() === now.toDateString();
    const currentHour = now.getHours();

    for (let i = 0; i < 24; i++) {
      // Jeśli to dzisiaj i godzina jest w przyszłości, nie generuj danych (lub daj 0)
      if (isToday && i > currentHour) {
         data.push({
          name: `${i}:00`,
          production: 0,
          consumption: 0,
          grid: 0,
        });
        continue;
      }

      const prod = Math.random() * 1.5;
      const cons = Math.random() * 0.8;
      data.push({
        name: `${i}:00`,
        production: prod,
        consumption: cons,
        grid: -(Math.random() * 1.0), 
      });
    }
    return data;
  }, [dailyDate]);

  const weeklyData = useMemo(() => {
    const days = ['Pon', 'Wt', 'Śr', 'Czw', 'Pt', 'Sob', 'Ndz'];
    const data: ChartData[] = [];
    
    // Ustalanie, który dzień tygodnia jest dzisiaj (0-6, gdzie 0 to niedziela)
    const now = new Date();
    // Konwersja na 0-6 gdzie 0 to poniedziałek, 6 to niedziela
    let currentDayIndex = now.getDay() - 1;
    if (currentDayIndex === -1) currentDayIndex = 6;

    // Sprawdzamy czy wyświetlany tydzień to bieżący tydzień
    const startOfViewWeek = new Date(weeklyDate);
    const day = startOfViewWeek.getDay();
    const diff = startOfViewWeek.getDate() - day + (day === 0 ? -6 : 1);
    startOfViewWeek.setDate(diff);
    startOfViewWeek.setHours(0,0,0,0);

    const startOfCurrentWeek = new Date(now);
    const cDay = startOfCurrentWeek.getDay();
    const cDiff = startOfCurrentWeek.getDate() - cDay + (cDay === 0 ? -6 : 1);
    startOfCurrentWeek.setDate(cDiff);
    startOfCurrentWeek.setHours(0,0,0,0);

    const isCurrentWeek = startOfViewWeek.getTime() === startOfCurrentWeek.getTime();

    days.forEach((dayName, index) => {
      // Jeśli to bieżący tydzień i dzień jest w przyszłości
      if (isCurrentWeek && index > currentDayIndex) {
        data.push({
          name: dayName,
          production: 0,
          consumption: 0,
          grid: 0,
        });
      } else {
        data.push({
          name: dayName,
          production: Math.random() * 20 + 10,
          consumption: Math.random() * 10 + 5,
          grid: -(Math.random() * 15 + 5),
        });
      }
    });
    
    return data;
  }, [weeklyDate]);

  return (
    <div className="charts-container">
      {/* Sekcja "Dziennie" */}
      <div className="chart-section">
        <div className="chart-header-centered">
          <h2>Dziennie</h2>
          <div className="date-selector">
            <span>Dane z {formatDate(dailyDate)}</span>
            <button className="icon-button" onClick={() => changeDay(-1)}><FaChevronLeft /></button>
            <button className="icon-button" onClick={() => changeDay(1)}><FaChevronRight /></button>
            <button className="icon-button" onClick={() => dailyDateInputRef.current?.showPicker()}>
              <FaCalendarAlt />
            </button>
            {/* Ukryty input do wyboru daty */}
            <input 
              type="date" 
              ref={dailyDateInputRef}
              style={{ visibility: 'hidden', position: 'absolute', width: 0 }}
              onChange={handleDailyDateChange}
            />
          </div>
        </div>
        <div className="chart-content">
          <ResponsiveContainer width="100%" height={300}>
            <BarChart data={dailyData} stackOffset="sign">
              <CartesianGrid strokeDasharray="3 3" stroke="#444" vertical={false} />
              <XAxis dataKey="name" stroke="#888" tick={{fontSize: 10}} interval={2} />
              <YAxis stroke="#888" tick={{fontSize: 10}} />
              <Tooltip 
                contentStyle={{ backgroundColor: '#333', border: 'none', color: '#fff' }}
                itemStyle={{ color: '#fff' }}
              />
              <ReferenceLine y={0} stroke="#666" />
              <Bar dataKey="production" name="Produkcja" stackId="a" fill="#3a7bfd" />
              <Bar dataKey="consumption" name="Zużycie" stackId="a" fill="#ff9800" />
              <Bar dataKey="grid" name="Sieć" stackId="a" fill="#9c27b0" />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>

      {/* Sekcja "Tygodniowo" */}
      <div className="chart-section">
        <div className="chart-header-centered">
          <h2>Tygodniowo</h2>
          <div className="date-selector">
            <span>Dane z {getWeekRange(weeklyDate)}</span>
            <button className="icon-button" onClick={() => changeWeek(-1)}><FaChevronLeft /></button>
            <button className="icon-button" onClick={() => changeWeek(1)}><FaChevronRight /></button>
            <button className="icon-button" onClick={() => weeklyDateInputRef.current?.showPicker()}>
              <FaCalendarAlt />
            </button>
             {/* Ukryty input do wyboru daty */}
             <input 
              type="date" 
              ref={weeklyDateInputRef}
              style={{ visibility: 'hidden', position: 'absolute', width: 0 }}
              onChange={handleWeeklyDateChange}
            />
          </div>
        </div>
        <div className="chart-content">
          <ResponsiveContainer width="100%" height={300}>
            <BarChart data={weeklyData} stackOffset="sign">
              <CartesianGrid strokeDasharray="3 3" stroke="#444" vertical={false} />
              <XAxis dataKey="name" stroke="#888" tick={{fontSize: 12}} />
              <YAxis stroke="#888" tick={{fontSize: 10}} />
              <Tooltip 
                contentStyle={{ backgroundColor: '#333', border: 'none', color: '#fff' }}
                itemStyle={{ color: '#fff' }}
              />
              <ReferenceLine y={0} stroke="#666" />
              <Bar dataKey="production" name="Produkcja" stackId="a" fill="#3a7bfd" />
              <Bar dataKey="consumption" name="Zużycie" stackId="a" fill="#ff9800" />
              <Bar dataKey="grid" name="Sieć" stackId="a" fill="#9c27b0" />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>
    </div>
  );
};

export default ChartsView;