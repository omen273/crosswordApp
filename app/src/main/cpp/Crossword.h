#pragma once

#include <optional>
#include <string>
#include <unordered_map>
#include <vector>

#include "CrosswordCell.h"
#include "LimitKeeper.h"
#include "Utils.h"

using crosswordString = std::basic_string<crosswordChar>;

// the scheme of crossword's axes and limits
//          (0, 0) (x)  top     width
//          ------------------------>
//      (y) |                       |
//          |               o       |
//          |               r       |
//  left    |            word       |   right
//          |               e       |
//          |               r       |
//          |                       |
//  height  |-----------------------|
//          v
//                  bottom
//          the first word "word" was inserted into width/2, height/2 position in the constructor,
//          you can use it as (0, 0) of your own axis with help of getCoordinateStart()

struct Word
{
    crosswordString  word;
    Utils::WordParams params;
};

struct CrosswordParams
{
    std::vector<Word> words;
    std::size_t width;
    std::size_t height;
};

class Crossword final
{
public:
    explicit Crossword(const crosswordString& firstWord,
            bool removeTouchesWithSameOrientation = true,
            std::size_t width = 30, std::size_t height = 30);
    [[nodiscard]] std::vector<Utils::insertionParams> testWord(const crosswordString& word) const;
    void insertWord(const crosswordString& word, const Utils::WordParams& wordParam);
    void eraseWord(const crosswordString& word);
    [[nodiscard]] Utils::Position getCoordinateStart() const noexcept;
    [[nodiscard]] auto getLetterCount() const noexcept { return letterN; }
    [[nodiscard]] auto getWordCount() const noexcept { return words.size(); }
    [[nodiscard]] CrosswordParams getCrossword() const;

private:
    [[nodiscard]] std::optional<std::size_t> canBeInserted(const Utils::WordParams& params,
            const crosswordString& word) const noexcept;
    [[nodiscard]] bool outsideBorders(const Utils::WordParams& wordParams, std::size_t size) const;

    mutable std::unordered_map<std::pair<crosswordString, crosswordString>,
    std::vector<Utils::Intersection>, Utils::wordsHashOrdered> intersectionCache;

    std::vector<std::vector<CrosswordCell>> board;
    std::unordered_map<crosswordString, Utils::WordParams> words;
    LimitKeeper limitKeeper;
    std::size_t letterN = 0;
    std::size_t intersectionN = 0;
    bool removeTouchesWithSameOrientation;
    std::size_t width;
    std::size_t height;
};
