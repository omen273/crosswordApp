#pragma once

//TODO think about one parameter instead horizontalBorder and verticalBorder
using crosswordChar = char;

enum class CellOrientation : std::uint8_t
{
    NONE,
    VERTICAL,
    HORIZONTAL,
    BOTH
};

enum class BordersType : std::uint8_t
{
    NONE,
    INSIDE,
    BEGIN,
    END,
};

class CrosswordCell final
{
public:
    [[nodiscard]] auto orientation() const noexcept
    {
        if (horizontalBorder_ == BordersType::NONE && verticalBorder_ == BordersType::NONE)
        {
            return CellOrientation::NONE;
        }
        else if (horizontalBorder_ != BordersType::NONE && verticalBorder_ == BordersType::NONE)
        {
            return CellOrientation::HORIZONTAL;
        }
        else if (verticalBorder_ != BordersType::NONE && horizontalBorder_ == BordersType::NONE)
        {
            return CellOrientation::VERTICAL;
        }
        return CellOrientation::BOTH;
    }

    [[nodiscard]] auto letter() const noexcept { return letter_; }

    void removeHorizontalLetter() noexcept
    {
        if (orientation() == CellOrientation::HORIZONTAL) letter_ = ' ';
        horizontalBorder_ = BordersType::NONE;
    }

    void removeVerticalLetter() noexcept
    {
        if (orientation() == CellOrientation::VERTICAL) letter_ = ' ';
        verticalBorder_ = BordersType::NONE;
    }

    void addHorizontalBeginLetter(crosswordChar letter) noexcept
    {
        addHorizontalLetter(letter, BordersType::BEGIN);
    }
    void addHorizontalInsideLetter(crosswordChar letter) noexcept
    {
        addHorizontalLetter(letter, BordersType::INSIDE);
    }
    void addHorizontalEndLetter(crosswordChar letter) noexcept
    {
        addHorizontalLetter(letter, BordersType::END);
    }
    void addVerticalBeginLetter(crosswordChar letter) noexcept
    {
        addVerticalLetter(letter, BordersType::BEGIN);
    }
    void addVerticalInsideLetter(crosswordChar letter) noexcept
    {
        addVerticalLetter(letter, BordersType::INSIDE);
    }
    void addVerticalEndLetter(crosswordChar letter) noexcept
    {
        addVerticalLetter(letter, BordersType::END);
    }

    [[nodiscard]] auto isVerticalBegin() const noexcept
    {
        return verticalBorder_ == BordersType::BEGIN;
    }
    [[nodiscard]] auto isVerticalEnd() const noexcept
    {
        return verticalBorder_ == BordersType::END;
    }
    [[nodiscard]] auto isHorizontalBegin() const noexcept
    {
        return horizontalBorder_ == BordersType::BEGIN;
    }
    [[nodiscard]] auto isHorizontalEnd() const noexcept
    {
        return horizontalBorder_ == BordersType::END;
    }

private:
    void addHorizontalLetter(crosswordChar letter, BordersType type) noexcept
    {
        if (letter_ != letter) verticalBorder_ = BordersType::NONE;
        letter_ = letter, horizontalBorder_ = type;
    }

    void addVerticalLetter(crosswordChar letter, BordersType type) noexcept
    {
        if (letter_ != letter) horizontalBorder_ = BordersType::NONE;
        letter_ = letter, verticalBorder_ = type;
    }

    crosswordChar letter_ = ' ';
    BordersType horizontalBorder_ = BordersType::NONE;
    BordersType verticalBorder_ = BordersType::NONE;
};
