package org.example

import java.io.File
import kotlin.random.Random

// ANSI-коды для цвета
const val RESET = "\u001B[0m"
const val RED = "\u001B[31m"
const val GREEN = "\u001B[32m"
const val YELLOW = "\u001B[33m"
const val BLUE = "\u001B[34m"

// Перечисление ходов с отображаемым названием и ASCII-графикой
enum class Move(val displayName: String, val art: String) {
    ROCK("Камень", """
         _______
    ---'   ____)
          (_____)
          (_____)
          (____)
    ---.__(___)
    """.trimIndent()),
    PAPER("Бумага", """
         _______
    ---'    ____)____
               ______)
              _______)
             _______)
    ---.__________)
    """.trimIndent()),
    SCISSORS("Ножницы", """
         _______
    ---'   ____)____
              ______)
           __________)
          (____)
    ---.__(___)
    """.trimIndent());

    companion object {
        fun from(input: String): Move? {
            return when(input.trim().lowercase()) {
                "камень", "rock", "1" -> ROCK
                "ножницы", "scissors", "2" -> SCISSORS
                "бумага", "paper", "3" -> PAPER
                else -> null
            }
        }
    }
}

// Режимы игры: один игрок или два игрока
enum class GameMode {
    SINGLE, MULTI
}

// Структура для хранения результатов раундов
data class RoundResult(
    val roundNumber: Int,
    val player1Move: Move,
    val player2Move: Move,
    val result: String // "Победа игрока 1", "Победа игрока 2" или "Ничья"
)

fun main() {
    println("${BLUE}Игра «Камень-Ножницы-Бумага»${RESET}")
    println("Выберите режим игры:")
    println("1. Один игрок (игра против компьютера)")
    println("2. Два игрока (игрок против игрока)")
    print("Ваш выбор: ")
    val modeInput = readLine()
    val gameMode = when (modeInput?.trim()) {
        "1" -> GameMode.SINGLE
        "2" -> GameMode.MULTI
        else -> {
            println("Неверный выбор, по умолчанию выбран режим: Один игрок")
            GameMode.SINGLE
        }
    }

    print("Введите количество раундов: ")
    val roundsInput = readLine()?.toIntOrNull() ?: 3
    val totalRounds = roundsInput.coerceAtLeast(1)

    var player1Wins = 0
    var player2Wins = 0
    var draws = 0

    val roundsList = mutableListOf<RoundResult>()

    for (round in 1..totalRounds) {
        println("\n${YELLOW}Раунд $round из $totalRounds${RESET}")

        // Получение хода первого игрока
        val player1Move = getPlayerMove(1)
        // Получение хода второго игрока или компьютера
        val player2Move = if (gameMode == GameMode.SINGLE) {
            getComputerMove()
        } else {
            getPlayerMove(2)
        }

        // Вывод выбора с использованием ASCII-графики и цвета
        println("\n${GREEN}Игрок 1 выбрал: ${player1Move.displayName}${RESET}")
        println(player1Move.art)
        if (gameMode == GameMode.SINGLE) {
            println("${RED}Компьютер выбрал: ${player2Move.displayName}${RESET}")
        } else {
            println("${RED}Игрок 2 выбрал: ${player2Move.displayName}${RESET}")
        }
        println(player2Move.art)

        // Определение результата раунда: 0 – ничья, 1 – победа игрока 1, -1 – победа игрока 2 (или компьютера)
        val result = determineRoundResult(player1Move, player2Move)
        when (result) {
            0 -> {
                println("${YELLOW}Ничья!${RESET}")
                draws++
            }
            1 -> {
                println("${GREEN}Победа игрока 1!${RESET}")
                player1Wins++
            }
            -1 -> {
                if (gameMode == GameMode.SINGLE) {
                    println("${RED}Победа компьютера!${RESET}")
                } else {
                    println("${RED}Победа игрока 2!${RESET}")
                }
                player2Wins++
            }
        }

        val roundResultStr = when (result) {
            0 -> "Ничья"
            1 -> "Победа игрока 1"
            -1 -> if (gameMode == GameMode.SINGLE) "Победа компьютера" else "Победа игрока 2"
            else -> ""
        }
        roundsList.add(RoundResult(round, player1Move, player2Move, roundResultStr))
    }

    // Вывод итоговой статистики
    println("\n${BLUE}Итоговая статистика:${RESET}")
    if (gameMode == GameMode.SINGLE) {
        println("Победы игрока: $player1Wins")
        println("Победы компьютера: $player2Wins")
    } else {
        println("Победы игрока 1: $player1Wins")
        println("Победы игрока 2: $player2Wins")
    }
    println("Ничьи: $draws")

    // Сохранение статистики в файл
    saveStatistics(roundsList, gameMode)

    println("\nСтатистика игры сохранена в файле 'game_stats.txt'")
}

/**
 * Запрашивает ход у игрока с проверкой корректности ввода.
 */
fun getPlayerMove(playerNumber: Int): Move {
    while (true) {
        print("Игрок $playerNumber, введите ваш выбор (1: Камень, 2: Ножницы, 3: Бумага): ")
        val input = readLine() ?: ""
        val move = Move.from(input)
        if (move != null) {
            return move
        } else {
            println("Неверный ввод. Попробуйте снова.")
        }
    }
}

/**
 * Генерирует случайный ход для компьютера.
 */
fun getComputerMove(): Move {
    return Move.values().random()
}

/**
 * Определяет результат раунда.
 * @return 0 для ничьей, 1 если победа игрока 1, -1 если победа игрока 2 (или компьютера).
 */
fun determineRoundResult(player1: Move, player2: Move): Int {
    if (player1 == player2) return 0
    return when(player1) {
        Move.ROCK -> if (player2 == Move.SCISSORS) 1 else -1
        Move.SCISSORS -> if (player2 == Move.PAPER) 1 else -1
        Move.PAPER -> if (player2 == Move.ROCK) 1 else -1
    }
}

/**
 * Сохраняет статистику игры в файл "game_stats.txt".
 */
fun saveStatistics(roundsList: List<RoundResult>, gameMode: GameMode) {
    val sb = StringBuilder()
    sb.append("Статистика игры \"Камень-Ножницы-Бумага\"\n")
    sb.append("Режим: ${if (gameMode == GameMode.SINGLE) "Один игрок (против компьютера)" else "Два игрока"}\n\n")
    for (round in roundsList) {
        sb.append("Раунд ${round.roundNumber}: Игрок 1 выбрал ${round.player1Move.displayName}, " +
                "${if (gameMode == GameMode.SINGLE) "Компьютер" else "Игрок 2"} выбрал ${round.player2Move.displayName} - ${round.result}\n")
    }
    val totalRounds = roundsList.size
    val winsPlayer1 = roundsList.count { it.result == "Победа игрока 1" }
    val winsPlayer2 = roundsList.count { it.result == "Победа компьютера" || it.result == "Победа игрока 2" }
    val draws = roundsList.count { it.result == "Ничья" }
    sb.append("\nИтоговая статистика:\n")
    if (gameMode == GameMode.SINGLE) {
        sb.append("Победы игрока: $winsPlayer1\n")
        sb.append("Победы компьютера: $winsPlayer2\n")
    } else {
        sb.append("Победы игрока 1: $winsPlayer1\n")
        sb.append("Победы игрока 2: $winsPlayer2\n")
    }
    sb.append("Ничьи: $draws\n")
    sb.append("Всего раундов: $totalRounds\n")

    File("game_stats.txt").writeText(sb.toString())
}
