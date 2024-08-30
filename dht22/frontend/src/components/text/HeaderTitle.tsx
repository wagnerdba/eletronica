import React from "react";
import '../../assets/css/styles.css';

const HeaderTitle: React.FC = () => {
  return (
    <div className="header-title-container">
      <h1 className="header-title">
        Temperatura e Umidade (ESP32 + DHT22)
      </h1>
      <p className="header-subtitle">
         Monitore tendências passadas e dados atuais em tempo real
      </p>
    </div>
  );
};

export default HeaderTitle;
