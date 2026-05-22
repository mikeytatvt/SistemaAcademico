DROP DATABASE IF EXISTS sistema_academico1;

CREATE DATABASE IF NOT EXISTS sistema_academico1
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE sistema_academico1;

CREATE TABLE IF NOT EXISTS alunos (
    rgm VARCHAR(20) PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    data_nascimento VARCHAR(10) NOT NULL,
    cpf VARCHAR(14) NOT NULL,
    email VARCHAR(100) NOT NULL,
    endereco VARCHAR(150) NOT NULL,
    municipio VARCHAR(50) NOT NULL,
    uf VARCHAR(2) NOT NULL,
    celular VARCHAR(15) NOT NULL,
    curso VARCHAR(50) NOT NULL,
    campus VARCHAR(50) NOT NULL,
    periodo VARCHAR(20) NOT NULL
);

CREATE TABLE IF NOT EXISTS notas_faltas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    rgm_aluno VARCHAR(20) NOT NULL,
    disciplina VARCHAR(100) NOT NULL,
    semestre VARCHAR(10) NOT NULL,
    nota DECIMAL(4,2) NOT NULL,
    faltas INT NOT NULL,
    CONSTRAINT uk_notas_faltas UNIQUE (rgm_aluno, disciplina, semestre),
    FOREIGN KEY (rgm_aluno) REFERENCES alunos(rgm) ON DELETE CASCADE,
    CHECK (nota >= 0 AND nota <= 10),
    CHECK (faltas >= 0 AND faltas <= 15)
);

INSERT INTO alunos
(rgm, nome, data_nascimento, cpf, email, endereco, municipio, uf, celular, curso, campus, periodo)
VALUES
('01290202', 'João Luis', '09/02/1997', '099.393.939-39', 'joao.luis@teste.com.br',
 'Rua Melo Peixoto, 100 apto 13', 'São Paulo', 'SP', '(11) 98292-8383',
 'Análise e Desenvolvimento de Sistemas', 'Tatuapé', 'Noturno')
ON DUPLICATE KEY UPDATE nome = VALUES(nome);

INSERT INTO notas_faltas (rgm_aluno, disciplina, semestre, nota, faltas)
VALUES
('01290202', 'Programação Orientada a Objetos', '2026-1', 8.50, 2),
('01290202', 'Banco de Dados I', '2026-1', 7.00, 4)
ON DUPLICATE KEY UPDATE nota = VALUES(nota), faltas = VALUES(faltas);
