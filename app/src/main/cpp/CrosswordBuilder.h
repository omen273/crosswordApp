#pragma once

#include <chrono>
#include <limits>
#include <optional>

#include "Crossword.h"

class CrosswordBuilder final
{
    //TODO think about moving of words or shared words
public:
    [[nodiscard]] static std::optional<Crossword> build(std::vector<std::string>& words,
            std::size_t wordCount, std::size_t maxSideSize = 30,
        std::chrono::milliseconds maxCalculationTime =
                std::chrono::milliseconds{ std::numeric_limits<long long>::max() });
};
